package com.part.livetaskcore.livatask

import androidx.lifecycle.LiveData
import com.part.livetaskcore.*
import com.part.livetaskcore.connection.ConnectionManager
import com.viam.resource.Resource
import com.viam.resource.onError
import com.viam.resource.onLoading
import com.viam.resource.onSuccess
import kotlinx.coroutines.*
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

internal const val DEFAULT_TIMEOUT = 100L

open class CoroutineLiveTask<T>(
    open val block: suspend LiveTaskBuilder<T>.() -> Unit = {}
) : BaseLiveTask<T>() {

    private var blockRunner: TaskRunner<T>? = null
    private var emittedSource: Emitted? = null
    private var connectionManager: ConnectionManager? = LiveTaskManager.getConnectionManager()
    private var noConnectionInformer: NoConnectionInformer? =
        LiveTaskManager.getNoConnectionInformer()
    var autoRetry = true
    private var errorObserver: ErrorObserverCallback = LiveTaskManager.getErrorObserver()
    private var errorMapper: ErrorMapper = LiveTaskManager.getErrorMapper()
    private var onSuccessAction: (T?) -> Unit = {}
    private var onErrorAction: (Exception) -> Unit = {}
    private var onLoadingAction: (Any?) -> Unit = {}
    var context: CoroutineContext? = null

    override fun runOn(coroutineContext: CoroutineContext?): LiveTask<T> {
        return run(coroutineContext ?: EmptyCoroutineContext)
    }

    override suspend fun run(): CoroutineLiveTask<T> {
        return run(currentCoroutineContext())
    }

    private fun run(coroutineContext: CoroutineContext): CoroutineLiveTask<T> {
        handleResult(Resource.Loading())
        val supervisorJob = SupervisorJob(coroutineContext[Job])
        context = Dispatchers.IO + coroutineContext + supervisorJob
        blockRunner = TaskRunner(
            liveData = this,
            block = block,
            timeoutInMs = DEFAULT_TIMEOUT,
            scope = CoroutineScope(context!!)
        ) {
            blockRunner = null
        }
        blockRunner?.maybeRun()
        return this
    }

    private fun retryOnNetworkBack() {
        connectionManager?.let {
            this.removeSource(it)
            this.addSource(it) { hasConnection ->
                if (hasConnection) {
                    retry()
                }
            }
        } ?: run {
            throw Exception("ConnectionManager has not been initialized. You must set up this class in your Application class or set autoRetry to false")
        }

    }

    override fun onActive() {
        super.onActive()
        blockRunner?.maybeRun()
    }

    override fun onInactive() {
        super.onInactive()
        blockRunner?.cancel()
    }

    override fun retry() {
        connectionManager?.let {
            this.removeSource(it)
        }
        run(context!!)
    }

    override fun cancel() {
        if (blockRunner == null) {
            handleResult(Resource.Error(CancellationException()))
        } else {
            blockRunner?.cancel()
        }
    }

    internal suspend fun emitSource(source: LiveData<Resource<T>>): DisposableHandle {
        clearSource()
        val newSource = addDisposableEmit(source)
        emittedSource = newSource
        return newSource
    }

    internal suspend fun clearSource() {
        emittedSource?.disposeNow()
        emittedSource = null
    }

    fun setErrorMapper(errorMapper: ErrorMapper) {
        if (this.errorMapper is ErrorMapperImpl) {
            this.errorMapper = errorMapper
        }
    }

    fun setErrorObserver(errorObserver: ErrorObserverCallback) {
        if (this.errorObserver is ErrorObserver) {
            this.errorObserver = errorObserver
        }
    }

    fun setSuccessAction(action: (T?) -> Unit) {
        onSuccessAction = action
    }

    fun setLoadingAction(action: (Any?) -> Unit) {
        onLoadingAction = action
    }

    fun setErrorAction(action: (Exception) -> Unit) {
        onErrorAction = action
    }

    fun handleResult(result: Resource<T>?) {
        print("applyResult:")
        println(result)
        result?.onSuccess {
            onSuccessAction(it)
            this.latestState = result
        }?.onError {
            it.printStackTrace()
            onErrorAction(it)
            broadcastError(it)
            if (it !is CancellationException)
                setResult(Resource.Error(errorMapper.mapError((result as Resource.Error).exception)))
            else {
                setResult(result)
            }

        }?.onLoading<T, Any?> {
            onLoadingAction(it)
            setResult(result)
        }

        this.postValue(this)
    }

    private fun setResult(result: Resource<T>) {
        this.latestState = result
    }

    private fun broadcastError(exception: Exception) {
        errorObserver.notifyError(ErrorEvent((exception)))
        when {
            noConnectionInformer?.invoke(exception) == true -> {
                if (autoRetry) retryOnNetworkBack()
            }
            else -> {
                if (retryCounts <= retryAttempts && exception !is CancellationException) {
                    retryCounts++
                    this.retry()
                }
            }
        }
    }

}
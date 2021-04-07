package com.part.livetaskcore.livatask

import androidx.lifecycle.LiveData
import com.part.livetaskcore.*
import com.part.livetaskcore.connection.ConnectionManager
import com.part.livetaskcore.utils.NoConnectionException
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
    private var errorObserver: ErrorObserverCallback = LiveTaskManager.getErrorObserver()
    private var errorMapper: ErrorMapper = LiveTaskManager.getErrorMapper()
    private var onSuccessAction: (T?) -> Unit = {}
    private var onErrorAction: (Exception) -> Unit = {}
    private var onLoadingAction: (Any?) -> Unit = {}
    var autoRetry = false

    override fun runOn(coroutineContext: CoroutineContext?): LiveTask<T> {
        return run(coroutineContext ?: EmptyCoroutineContext)
    }

    override suspend fun run(): CoroutineLiveTask<T> {
        return run(currentCoroutineContext())
    }

    private fun run(coroutineContext: CoroutineContext): CoroutineLiveTask<T> {
        applyResult(Resource.Loading())
        val supervisorJob = SupervisorJob(coroutineContext[Job])
        val scope = CoroutineScope(Dispatchers.IO + coroutineContext + supervisorJob)
        blockRunner = TaskRunner(
            liveData = this,
            block = block,
            timeoutInMs = DEFAULT_TIMEOUT,
            scope = scope
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
        applyResult(Resource.Loading())
        blockRunner?.maybeRun()
    }

    override fun cancel() {
        blockRunner?.cancel()
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

    fun applyResult(result: Resource<T>?) {
        this.latestState = result
        result?.onSuccess {
            onSuccessAction(it)
        }?.onError {
            onErrorAction(it)
        }?.onLoading<T, Any?> {
            onLoadingAction(it)
        }
        this.postValue(this)
        if (result is Resource.Error) applyError()
    }

    private fun applyError() {
        result()?.onError { exception ->
            errorObserver.notifyError(ErrorEvent((exception)))

            when (exception) {
                is NoConnectionException -> {
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

}
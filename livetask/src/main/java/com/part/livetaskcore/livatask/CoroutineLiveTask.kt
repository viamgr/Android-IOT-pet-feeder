package com.part.livetaskcore.livatask

import androidx.lifecycle.LiveData
import com.part.livetaskcore.*
import kotlinx.coroutines.*
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

open class CoroutineLiveTask<T>(
    open val block: suspend LiveTaskBuilder<T>.() -> Unit = {},
    private val liveTaskManager: LiveTaskManager = LiveTaskManager.instance,
) : BaseLiveTask<T>(liveTaskManager) {
    private var retryCounts = 1

    private var emittedSource: Emitted? = null
    private var connectionInformer: ConnectionInformer? = liveTaskManager.connectionInformer
    var context: CoroutineContext? = null


    override val isRetryable = retryable
    override val isAutoRetry = autoRetry
    override val isCancelable = cancelable

    internal suspend fun addDisposableEmit(
        source: LiveData<Resource<T>>,
    ): Emitted = withContext(Dispatchers.Main.immediate) {
        addSource(source) {
            latestState = it
            value = this@CoroutineLiveTask
        }
        Emitted(source = source, mediator = this@CoroutineLiveTask)
    }

    override fun run(coroutineContext: CoroutineContext?): LiveTask<T> {
        return run(coroutineContext ?: EmptyCoroutineContext)
    }

    override suspend fun run(): CoroutineLiveTask<T> {
        return run(currentCoroutineContext())
    }

    private fun run(coroutineContext: CoroutineContext): CoroutineLiveTask<T> {
        println("run new task")
        unRegisterConnectionInformer()
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

    override fun onActive() {
        super.onActive()
        blockRunner?.maybeRun()
    }

    override fun onInactive() {
        super.onInactive()
        println("onInactive function")
        blockRunner?.cancel()
    }

    override fun retry() {
        run(context!!)
    }

    private fun unRegisterConnectionInformer() {
        connectionInformer?.unregister(this)
    }

    override fun cancel() {
        println("cancel function")
        if (blockRunner == null) {
            handleResult(Resource.Error(CancellationException()))
        } else {
            blockRunner?.cancel()
        }
    }

    override suspend fun emitSource(source: LiveData<Resource<T>>): DisposableHandle {
        clearSource()
        val newSource = addDisposableEmit(source)
        emittedSource = newSource
        return newSource
    }

    private suspend fun clearSource() {
        emittedSource?.disposeNow()
        emittedSource = null
    }

    private fun handleResult(result: Resource<T>?) {
        print("applyResult:")
        println(result)
        unRegisterConnectionInformer()
        result?.onSuccess {
            onSuccessAction(it)
            setResult(result)
        }?.onError {
            it.printStackTrace()
            onErrorAction(it)
            if (it !is CancellationException) {
                println(("it !is CancellationException"))
                setResult(Resource.Error(errorMapper?.mapError(it) ?: it))
            } else {
                println(("it is CancellationException"))
                setResult(result)
            }
            handleAutoRetry(it)
        }?.onLoading<T, Any?> {
            onLoadingAction(it)
            setResult(result)
        }

        this.postValue(this)
    }

    private fun handleResult(result: T) {
        unRegisterConnectionInformer()
        val mappedResult = liveTaskManager.resourceMapper.map(result)
        mappedResult.onSuccess {
            onSuccessAction(result)
            setResult(Resource.Success(it) as Resource<T>)

        }.onError {
            it.printStackTrace()
            onErrorAction(it)
            if (it !is CancellationException) {
                println(("it !is CancellationException"))
                setResult(Resource.Error(errorMapper?.mapError(it) ?: it))
            } else {
                println(("it is CancellationException"))
            }
            handleAutoRetry(it)
            setResult(Resource.Error(it))
        }.onLoading<T, Any?> {
            onLoadingAction(it)
            setResult(Resource.Loading(it))
        }

        this.postValue(this)
    }

    private fun setResult(result: Resource<T>) {
        println("setResult: $result")
        this.latestState = result
    }

    private fun handleAutoRetry(exception: Exception) {
        if (autoRetry == true && exception !is CancellationException) {
            connectionInformer?.register(exception, this)
        }
    }

    override suspend fun emit(result: Resource<T>) {
        clearSource()
        withContext(context!! + Dispatchers.Main.immediate) {
            println("emit $result")
            handleResult(result)
        }
    }

    override suspend fun emit(result: T) {
        clearSource()
        withContext(context!! + Dispatchers.Main.immediate) {
            println("emit $result")
            handleResult(result)
        }
    }


}
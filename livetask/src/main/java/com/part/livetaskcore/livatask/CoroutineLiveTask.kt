package com.part.livetaskcore.livatask

import androidx.lifecycle.LiveData
import com.part.livetaskcore.*
import com.part.livetaskcore.connection.ConnectionInformer
import kotlinx.coroutines.*
import kotlin.coroutines.CoroutineContext

open class CoroutineLiveTask<T>(
    private val liveTaskManager: LiveTaskManager = LiveTaskManager.instance,
    open val block: LiveTaskBuilder<T>.() -> Unit = {},
) : BaseLiveTask<T>(liveTaskManager) {
    private var emitBlock: Any? = null
    private var emittedSource: Emitted? = null
    private var connectionInformer: ConnectionInformer? = liveTaskManager.connectionInformer
    var context: CoroutineContext? = null


    override fun configure() {
        block.invoke(this@CoroutineLiveTask)
    }

    override fun isRetryable(): Boolean = retryable == true
    override fun isAutoRetry() = autoRetry == true
    override fun isCancelable() = cancelable == true

    private suspend fun addDisposableEmit(
        source: LiveData<Resource<T>>,
    ): Emitted = withContext(Dispatchers.Main.immediate) {
        addSource(source) {
            setResult(it)
            value = this@CoroutineLiveTask
        }
        Emitted(source = source, mediator = this@CoroutineLiveTask)
    }

    override fun run(coroutineContext: CoroutineContext): LiveTask<T> {
        return runTask(coroutineContext)
    }

    override fun onErrorHappened(value: Resource.Error) {
        super.onErrorHappened(value)
        handleAutoRetry(value.exception)
    }

    override suspend fun run(): CoroutineLiveTask<T> {
        return runTask(currentCoroutineContext())
    }

    private fun runTask(coroutineContext: CoroutineContext): CoroutineLiveTask<T> {
        emitBlock = null
        configure()
        unRegisterConnectionInformer()
        applyResult(Resource.Loading())
        val supervisorJob = SupervisorJob(coroutineContext[Job])
        context = coroutineContext + supervisorJob

        blockRunner = TaskRunner(
            liveData = this,
            block = {
                onRunCallback?.invoke()
                emitBlock?.let {
                    if (it is EmitDataBlock<*>) {
                        val data = it.invoke()
                        @Suppress("UNCHECKED_CAST")
                        emitWithClear(getMappedResult(data as T))
                    } else if (it is EmitResultBlock<*>) {
                        @Suppress("UNCHECKED_CAST")
                        emitWithClear(it.invoke() as Resource<T>)
                    }
                }
            },
            timeoutInMs = DEFAULT_TIMEOUT,
            scope = CoroutineScope(context!!)
        ) {
            blockRunner = null
        }
        blockRunner?.maybeRun()
        return this
    }

    override fun applyResult(result: Resource<T>) {
        unRegisterConnectionInformer()
        super.applyResult(result)
    }

    override fun onActive() {
        super.onActive()
        blockRunner?.maybeRun()
    }

    override fun onInactive() {
        super.onInactive()
        println("onInactive function")
        blockRunner?.cancel(false)
    }

    override fun retry() {
        if (context == null) {
            throw Exception("You shouldn't retry before calling run")
        }
        run(context!!)
    }

    private fun unRegisterConnectionInformer() {
        if (isAutoRetry())
            connectionInformer?.unregister(this)
    }

    override fun cancel(immediately: Boolean?): LiveTask<T> {
        if (blockRunner == null) {
            applyResult(Resource.Error(CancellationException()))
        } else {
            blockRunner?.cancel(immediately == true)
        }
        return this
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


    private fun handleAutoRetry(exception: Exception) {
        if (autoRetry == true && exception !is CancellationException) {
            connectionInformer?.register(exception, this)
        }
    }

    override fun emitResult(resultBlock: EmitResultBlock<T>) {
        emitBlock = resultBlock
    }

    override fun emitData(dataBlock: EmitDataBlock<T>) {
        emitBlock = dataBlock
    }

    private suspend fun emitWithClear(result: Resource<T>) {
        clearSource()
        applyResult(result)
    }

    private fun getMappedResult(data: T): Resource<T> {
        val map = resourceMapper?.map(data)
        val resource = (map ?: Resource.Success(data))
        @Suppress("UNCHECKED_CAST")
        return resource as Resource<T>
    }

    override suspend fun emit(data: T) {
        emitWithClear(getMappedResult(data))
    }
}
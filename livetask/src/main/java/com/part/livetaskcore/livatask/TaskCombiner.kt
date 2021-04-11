package com.part.livetaskcore.livatask

import com.part.livetaskcore.ErrorMapper
import com.part.livetaskcore.ErrorObserverCallback
import com.part.livetaskcore.LiveTaskManager
import com.viam.resource.Resource
import com.viam.resource.withResult
import kotlinx.coroutines.*
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

/**
 * Combines multiple [LiveTask]s that executes on the given [block] and in the specified.
 * */
class TaskCombiner(
    private vararg val requests: LiveTask<*>,
    val block: suspend CombinerBuilder.() -> Unit,
    liveTaskManager: LiveTaskManager = LiveTaskManager.instance
) : BaseLiveTask<Any>() {
    private var errorMapper: ErrorMapper? = liveTaskManager.getErrorMapper()
    private var combineRunner: CombineRunner? = null
    private var onSuccessAction: (Any?) -> Unit = {}
    private var onErrorAction: (Exception) -> Unit = {}
    private var onLoadingAction: (Any?) -> Unit = {}

    init {
        requests.forEach { addTaskAsSource(it) }
    }

    private fun addTaskAsSource(task: LiveTask<*>) {
        val asLiveData = task.asLiveData()
        this.addSource(asLiveData) {
            var cancelCount = 0
            var successCount = 0
            var loadingCount = 0
            val exceptions = mutableListOf<Exception>()
            requests.forEach {
                val result = it.result()
                if (result is Resource.Error) {
                    if (result.exception !is CancellationException)
                        exceptions.add(result.exception)
                    else {
                        cancelCount++
                    }
                } else if (result is Resource.Success) {
                    successCount++
                } else if (result is Resource.Loading) {
                    loadingCount++
                } else {
                    cancelCount++
                }
            }


            println("progress: successCount:$successCount loadingCount:$loadingCount cancelCount:$cancelCount exceptions:${exceptions.size} exceptions:$exceptions")
            when {
                exceptions.isNotEmpty() -> {
                    val exception = CombinedException(exceptions)
                    applyResult(Resource.Error(errorMapper?.mapError(exception) ?: exception))
                }
                successCount == requests.size -> {
                    applyResult(Resource.Success(null))
                }
                cancelCount == requests.size -> {
                    applyResult(Resource.Error(CancellationException()))
                }
                loadingCount > 0 -> {
                    applyResult(Resource.Loading(null))
                }
                else -> {
                    applyResult(Resource.Error(CancellationException()))
                }
            }

        }
    }

    override fun retry() {
        applyResult(Resource.Loading())
        requests.filter { coroutineLiveTask ->
            coroutineLiveTask.result() is Resource.Error && (coroutineLiveTask.result() as Resource.Error).exception !is CancellationException
        }.forEach { coroutineLiveTask ->
            coroutineLiveTask.retry()
        }
    }

    override suspend fun run(): LiveTask<Any> {
        val context = currentCoroutineContext()
        return run(context)
    }

    private fun run(context: CoroutineContext): TaskCombiner {
        val supervisorJob = SupervisorJob(context[Job])
        val scope = CoroutineScope(Dispatchers.IO + context + supervisorJob)
        combineRunner = CombineRunner(
            liveData = this,
            block = block,
            timeoutInMs = DEFAULT_TIMEOUT,
            scope = scope
        ) {
            combineRunner = null
        }

        combineRunner?.maybeRun()

        requests.forEach { it.runOn(context) }
        return this
    }

    fun setRetryAttemptsTasks(attempts: Int) {
        requests.forEach {
            it as CoroutineLiveTask
            it.retryAttempts = attempts
        }
    }

    fun setAutoRetryForAll(bool: Boolean) {
        requests.forEach {
            it as CoroutineLiveTask
            it.autoRetry = bool
        }
    }

    fun setErrorMapper(errorMapper: ErrorMapper) {
        this.errorMapper = errorMapper
    }

    fun setErrorObserver(errorObserver: ErrorObserverCallback) {
        requests.forEach {
            it as CoroutineLiveTask
            it.setErrorObserver(errorObserver)
        }
    }

    override fun cancel() {
        requests.filter { coroutineLiveTask ->
            coroutineLiveTask.result() !is Resource.Success
        }.forEach {
            it.cancel()
        }
    }

    fun setSuccessAction(action: (Any?) -> Unit) {
        onSuccessAction = action
    }

    fun setLoadingAction(action: (Any?) -> Unit) {
        onLoadingAction = action
    }

    fun setErrorAction(action: (Exception) -> Unit) {
        onErrorAction = action
    }

    private fun applyResult(result: Resource<Any>) {
        this.latestState = result
        result.withResult(
            onSuccess = { onSuccessAction(it) },
            onError = { onErrorAction(it) },
            onLoading = { onLoadingAction(it) }
        )
        postValue(this)
    }

    private fun applyResult(task: LiveTask<*>) {
        @Suppress("UNCHECKED_CAST")
        this.latestState = task.result() as Resource<Any>?
        postValue(this)
    }

    override fun runOn(coroutineContext: CoroutineContext?): LiveTask<Any> {
        return run(coroutineContext ?: EmptyCoroutineContext)
    }
}
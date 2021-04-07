package com.part.livetaskcore.livatask

import com.part.livetaskcore.ErrorMapper
import com.part.livetaskcore.ErrorObserverCallback
import com.part.livetaskcore.utils.NoConnectionException
import com.viam.resource.Resource
import com.viam.resource.withResult
import kotlinx.coroutines.*
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

/**
 * Combines multiple [LiveTask]s that executes on the given [block] and in the specified.
 * */
class TaskCombiner(
    vararg requests: LiveTask<*>,
    val block: suspend CombinerBuilder.() -> Unit
) : BaseLiveTask<Any>() {

    private var taskList = mutableListOf<LiveTask<*>>()
    private var combineRunner: CombineRunner? = null
    private var onSuccessAction: (Any?) -> Unit = {}
    private var onErrorAction: (Exception) -> Unit = {}
    private var onLoadingAction: (Any?) -> Unit = {}

    init {
        requests.forEach { addTaskAsSource(it) }
    }

    private fun addTaskAsSource(task: LiveTask<*>) {
        taskList.add(task)
        val asLiveData = task.asLiveData()
        this.addSource(asLiveData) { liveTask ->
            when (val result = liveTask.result()) {
                is Resource.Success -> {
                    checkIsThereAnyUnSuccess()
                }
                is Resource.Error -> {
                    if (this.value?.result() !is Resource.Error && result.exception !is CancellationException) {

                        when (result.exception) {
                            is NoConnectionException, is CancellationException -> {
                                applyResult(task)
                            }
                            else -> {
                                @Suppress("UNCHECKED_CAST")
                                task as BaseLiveTask<Any>
                                if ((task).retryCounts > task.retryAttempts) {
                                    applyResult(task.result())
                                }
                            }
                        }
                    } else {
                        checkIsThereAnyUnSuccess()
                    }
                }
                is Resource.Loading -> {
                    when (this.value?.result()) {
                        is Resource.Error -> {
                            setLoadingIfNoErrorLeft()
                        }
                        is Resource.Success<*> -> {
                            applyResult(Resource.Loading())
                        }
                        is Resource.Loading -> {
                        }
                        else -> {
                            applyResult(Resource.Loading())
                        }
                    }
                }
            }
        }
    }

    private fun setLoadingIfNoErrorLeft() {
        val hasError = taskList.any {
            it.result() is Resource.Error && (it.result() as Resource.Error).exception !is CancellationException
        }
        if (!hasError) {
            applyResult(Resource.Loading())
        }
    }

    private fun checkIsThereAnyUnSuccess() {
        val anyRequestLeft = taskList.any {
            it.result() is Resource.Loading || (it.result() is Resource.Error && (it.result() as Resource.Error).exception !is CancellationException)
        }
        if (!anyRequestLeft) {
            applyResult(Resource.Success(Any()))
        } else {
            if (this.value?.result() is Resource.Loading) {
                val oneOfErrors = taskList.find { coroutineLiveTask ->
                    coroutineLiveTask.result() is Resource.Error && (coroutineLiveTask.result() as Resource.Error).exception !is CancellationException
                }
                oneOfErrors?.let {
                    applyResult(it)
                }
            }
        }
    }

    override fun retry() {
        applyResult(Resource.Loading())
        val listOfUnSuccesses = taskList.filter { coroutineLiveTask ->
            coroutineLiveTask.result() is Resource.Error && (coroutineLiveTask.result() as Resource.Error).exception !is CancellationException
        }
        listOfUnSuccesses.forEach { coroutineLiveTask ->
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

        taskList.forEach { it.runOn(context) }
        return this
    }

    fun setRetryAttemptsTasks(attempts: Int) {
        taskList.forEach {
            it as CoroutineLiveTask
            it.retryAttempts = attempts
        }
    }

    fun setAutoRetry(bool: Boolean) {
        taskList.forEach {
            it as CoroutineLiveTask
            it.autoRetry = bool
        }
    }

    fun setErrorMapper(errorMapper: ErrorMapper) {
        taskList.forEach {
            it as CoroutineLiveTask
            it.setErrorMapper(errorMapper)
        }
    }

    fun setErrorObserver(errorObserver: ErrorObserverCallback) {
        taskList.forEach {
            it as CoroutineLiveTask
            it.setErrorObserver(errorObserver)
        }
    }

    override fun cancel() {
        val listOfUnLoading = taskList.filter { coroutineLiveTask ->
            coroutineLiveTask.result() is Resource.Loading
        }

        listOfUnLoading.forEach { coroutineLiveTask ->
            coroutineLiveTask.cancel()
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

    private fun applyResult(result: Resource<Any>?) {
        this.latestState = result
        result?.withResult(
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

    fun printStats() {
        var successes = 0
        var failed = 0
        var loading = 0
        taskList.forEach { coroutineLiveTask ->
            when (coroutineLiveTask.result()) {
                is Resource.Success -> {
                    successes++
                }
                is Resource.Error -> {
                    failed++
                }
                is Resource.Loading -> {
                    loading++
                }
            }
        }
        println("mmb successful: $successes failed :$failed loading :$loading all requests: ${taskList.size}")
    }

    override fun runOn(coroutineContext: CoroutineContext?): LiveTask<Any> {
        return run(coroutineContext ?: EmptyCoroutineContext)
    }
}
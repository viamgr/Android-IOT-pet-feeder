package com.part.livetaskcore.livatask

import androidx.lifecycle.LiveData
import com.part.livetaskcore.LiveTaskManager
import com.part.livetaskcore.Resource
import kotlinx.coroutines.DisposableHandle
import kotlinx.coroutines.currentCoroutineContext
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.cancellation.CancellationException

/**
 * Combines multiple [LiveTask]s that executes on the given [block] and in the specified.
 * */
class TaskCombiner(
    private vararg val requests: LiveTask<*>,
    val block: (LiveTaskBuilder<Any>.() -> Unit)? = null,
    liveTaskManager: LiveTaskManager = LiveTaskManager.instance,
) : BaseLiveTask<Any>(liveTaskManager) {
    init {
        requests.forEach { addTaskAsSource(it) }
        block?.invoke(this)
    }

    override val isCancelable: Boolean
        get() = cancelable ?: !(requests.all { it.isCancelable == false })

    override val isRetryable: Boolean
        get() = retryable ?: requests.any { it.isRetryable == true }

    override val isAutoRetry: Boolean
        get() = retryable ?: requests.any { it.isAutoRetry == true }


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
                    applyResult(Resource.Success(Unit))
                }
                cancelCount == requests.size -> {
                    applyResult(Resource.Error(CancellationException()))
                }
                loadingCount > 0 -> {
                    applyResult(Resource.Loading())
                }
                else -> {
                    applyResult(Resource.Error(CancellationException()))
                }
            }

        }
    }


    private fun run(context: CoroutineContext): TaskCombiner {
        requests.forEach { it.run(context) }
        return this
    }

    override fun retry() {
        requests.filter { coroutineLiveTask -> hasError(coroutineLiveTask) }
            .forEach { coroutineLiveTask -> coroutineLiveTask.retry() }
    }


    override suspend fun run(): LiveTask<Any> {
        return run(currentCoroutineContext())
    }

    override fun run(coroutineContext: CoroutineContext?): LiveTask<Any> {
        run(coroutineContext)
        return this
    }

    override fun cancel() {
        requests.filter { coroutineLiveTask ->
            coroutineLiveTask.result() is Resource.Loading
        }.forEach { coroutineLiveTask ->
            coroutineLiveTask.cancel()
        }
    }

    override suspend fun emit(result: Resource<Any>) {
        throw IllegalStateException()
    }

    override suspend fun emitSource(source: LiveData<Resource<Any>>): DisposableHandle {
        throw IllegalStateException()
    }

    override suspend fun emit(result: Any) {
        throw IllegalStateException()
    }

}
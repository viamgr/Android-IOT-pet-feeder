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
class CombinedLiveTask(
    private vararg val liveTasks: LiveTask<*>,
    liveTaskManager: LiveTaskManager = LiveTaskManager.instance,
    val block: (LiveTaskBuilder<Any>.() -> Unit)? = null,
) : BaseLiveTask<Any>(liveTaskManager) {
    init {
        liveTasks.forEach {
            it.configure()
            addTaskAsSource(it)
        }
        block?.invoke(this)
    }

    override fun isCancelable() = cancelable ?: !(liveTasks.all { !it.isCancelable() })

    override fun isRetryable(): Boolean =
        retryable != false && liveTasks.any { canRetry(it) }

    override fun isAutoRetry() = autoRetry ?: liveTasks.any { it.isAutoRetry() }

    private fun canRetry(liveTask: LiveTask<*>) =
        liveTask.isRetryable() && liveTask.result() is Resource.Error && (liveTask.result() as Resource.Error).exception !is CancellationException


    private fun addTaskAsSource(task: LiveTask<*>) {
        val asLiveData = task.asLiveData()
        //TODO fix compose errors
        this.addSource(asLiveData) {
            var cancelCount = 0
            var successCount = 0
            var loadingCount = 0
            val exceptions = mutableListOf<Exception>()
            liveTasks.forEach {
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
            println("cancelCount $cancelCount")
            println("successCount $successCount")
            println("loadingCount $loadingCount")
            when {
                exceptions.isNotEmpty() -> {
                    val exception = CombinedException(exceptions)
                    applyResult(Resource.Error(exception))
                }
                successCount == liveTasks.size -> {
                    applyResult(Resource.Success(Unit))
                }
                cancelCount == liveTasks.size -> {
                    applyResult(Resource.Error(CancellationException()))
                }
                loadingCount > 0 -> {
                    val data: List<Resource<Any?>?> = liveTasks.map { it.result() }
                    applyResult(Resource.Loading(data))
                }
                else -> {
                    applyResult(Resource.Error(CancellationException()))
                }
            }

        }
    }


    private fun runTasks(context: CoroutineContext): CombinedLiveTask {
        liveTasks.forEach { it.run(context) }
        return this
    }

    override fun retry() {
        liveTasks.filter { coroutineLiveTask -> canRetry(coroutineLiveTask) }
            .forEach { coroutineLiveTask -> coroutineLiveTask.retry() }
    }


    override suspend fun run(): LiveTask<Any> {
        return runTasks(currentCoroutineContext())
    }

    override fun run(coroutineContext: CoroutineContext): LiveTask<Any> {
        runTasks(coroutineContext)
        return this
    }

    override fun cancel(immediately: Boolean?): LiveTask<Any> {
        liveTasks.filter { coroutineLiveTask ->
            (coroutineLiveTask.result() is Resource.Loading || coroutineLiveTask.result() is Resource.Error)
                    || coroutineLiveTask is CombinedLiveTask

        }.forEach { coroutineLiveTask ->
            coroutineLiveTask.cancel(immediately)
        }
        return this
    }

    override suspend fun emitSource(source: LiveData<Resource<Any>>): DisposableHandle {
        throw IllegalStateException("You are not allowed to call 'emitSource' function inside combine CombinedLiveTask builder")
    }

    override fun emitResult(resultBlock: EmitResultBlock<Any>) {
        throw IllegalStateException("You are not allowed to call 'emitBlock' function inside combine CombinedLiveTask builder")
    }

    override fun emitData(dataBlock: EmitDataBlock<Any>) {
        throw IllegalStateException("You are not allowed to call 'emitData' function inside combine CombinedLiveTask builder")
    }

    override fun configure() {
        liveTasks.forEach { it.configure() }
    }

    override suspend fun emit(data: Any) {
        throw IllegalStateException("You are not allowed to call 'emit' function inside combine CombinedLiveTask builder")
    }

    override fun emitResult(resource: Resource<Any>) {
        throw IllegalStateException("Not yet implemented")
    }
}
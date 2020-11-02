package com.viam.feeder.core.task

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.viam.feeder.core.Resource
import kotlinx.coroutines.CompletableJob
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class Request<R, T>(
    private var requestBlock: TaskBuilder<R, T>.() -> Unit
) : TaskBuilder<R, T>, PromiseTask<R, T> {
    private val _result = MutableLiveData<PromiseTask<R, T>?>()
    val result: LiveData<PromiseTask<R, T>?> = _result
    private var logger: EventLogger? = TaskEventLogger
    private var state: Resource<T>? = null
    private var params: R? = null
    private var job: CompletableJob? = null

    override suspend fun execute(block: suspend (R) -> Resource<T>) {
        setResult(Resource.Loading)
        job?.cancel()
        job = SupervisorJob().also { completableJob ->
            CoroutineScope(completableJob).launch {
                params?.let {
                    setResult(block.invoke(it))
                }
            }
        }
    }

    override fun request(params: R) {
        this.params = params
        if (job == null || job?.isCancelled == true || job?.isCompleted == true
            || state is Resource.Error
        ) {
            requestBlock.invoke(this)
        }
    }

    override fun status() = state

    override fun cancel() {
        job?.cancel()
        setResult(null)
    }

    override fun retry() {
        params?.let {
            request(it)
        }
    }

    override fun initialParams(params: R) {
        this.params = params
    }

    override fun result(): LiveData<PromiseTask<R, T>?> {
        return result
    }

    override suspend fun logger(logger: EventLogger) {
        this.logger = logger
    }

    private fun setResult(resource: Resource<T>?) {
        logger?.newEvent(resource)
        state = resource
        _result.postValue(this)
    }
}

fun <R, T : Any> makeRequest(requestBlock: TaskBuilder<R, T>.() -> Unit): PromiseTask<R, T> {
    return Request(requestBlock)
}
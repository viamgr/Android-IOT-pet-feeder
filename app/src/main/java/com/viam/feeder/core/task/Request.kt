package com.viam.feeder.core.task

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.viam.feeder.core.Resource
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext

class Request<R, T>(
    private var requestBlock: RequestBuilder<R, T>.() -> Unit
) : RequestBuilder<R, T>, PromiseTask<R, T> {
    private val _result = MutableLiveData<PromiseTask<R, T>?>()
    val result: LiveData<PromiseTask<R, T>?> = _result
    private var state: Resource<T>? = null
    private var params: R? = null
    private var currentJob: Job? = null

    override fun withContext(
        coroutineContext: CoroutineContext, block: suspend (R) -> Unit
    ) {
        state = Resource.Loading?.also {
            _result.postValue(this)
        }
        currentJob = CoroutineScope(coroutineContext).launch {
            params?.let {
                block.invoke(it)
            }
        }
    }

    override fun emit(resultValue: Resource<T>) {
        state = resultValue
        _result.postValue(this)
    }

    override fun execute(params: R) {
        this.params = params
        if (currentJob == null || currentJob?.isCancelled == true || currentJob?.isCompleted == true) {
            requestBlock.invoke(this)
        }
    }

    override fun status() = state

    override fun cancel() {
        currentJob?.cancel()
        state = null
        _result.postValue(this)
    }

    override fun retry() {
        params?.let {
            execute(it)
        }
    }

    override fun initialParams(params: R) {
        this.params = params
    }

    override fun result(): LiveData<PromiseTask<R, T>?> {
        return result
    }

}

fun <R, T : Any> makeRequest(requestBlock: RequestBuilder<R, T>.() -> Unit): PromiseTask<R, T> {
    return Request(requestBlock)
}

fun compositeTask(
    globalRequest: GlobalRequest,
    vararg requests: PromiseTask<*, *>
): PromiseTask<Any, Any> {
    return CompositeRequest(globalRequest, *requests)
}

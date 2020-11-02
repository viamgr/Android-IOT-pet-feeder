package com.viam.feeder.core.task

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import com.viam.feeder.core.Resource

class CompositeRequest(
    requestBlock: (CompositeTaskBuilder.() -> Unit)?,
    private vararg val requests: PromiseTask<*, *>
) : PromiseTask<Any, Any>, CompositeTaskBuilder {
    private var logger: EventLogger? = null
    private val _result = MediatorLiveData<PromiseTask<Any, Any>?>()
    val result: LiveData<PromiseTask<Any, Any>?> = _result
    private var state: Resource<Any>? = null

    init {
        requestBlock?.invoke(this)
        requests.map { request ->
            _result.addSource(request.result()) {
                logger?.newEvent(request.status())
                val notSuccessList = requests
                    .filterNot { it.status() == null || it.status() is Resource.Success }
                    .map { it.status() }
                if (notSuccessList.isEmpty()) {
                    state = Resource.Success(Unit)
                    _result.value = this
                } else {
                    if (!notSuccessList.any { it is Resource.Error }) {
                        state = Resource.Loading
                        _result.value = this
                    } else {
                        val errors = notSuccessList.filterIsInstance(Resource.Error::class.java)
                            .map { it.exception }
                        state = Resource.Error(CompositeException(errors))
                        _result.value = this
                    }
                }
            }
        }
    }

    override fun retry() {
        requests.map {
            it.retry()
        }
    }

    override fun status() = state

    override fun cancel() {
        requests.map {
            it.cancel()
        }
    }

    override fun request(params: Any) {
        requests.map {
            it.retry()
        }
    }

    override fun result(): LiveData<PromiseTask<Any, Any>?> {
        return result
    }

    override suspend fun logger(logger: EventLogger) {
        this.logger = logger
    }

}

fun compositeTask(
    vararg requests: PromiseTask<*, *>,
    requestBlock: (CompositeTaskBuilder.() -> Unit)? = null
): PromiseTask<Any, Any> {
    return CompositeRequest(requestBlock, *requests)
}
package com.viam.feeder.core.task

import androidx.hilt.Assisted
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import com.viam.feeder.core.Resource
import javax.inject.Inject

class CompositeRequest @Inject constructor(
    private val globalRequest: GlobalRequest,
    @Assisted vararg val requests: PromiseTask<*, *>
) : PromiseTask<Any, Any> {
    private val _result = MediatorLiveData<PromiseTask<Any, Any>?>()
    val result: LiveData<PromiseTask<Any, Any>?> = _result
    private var state: Resource<Any>? = null

    init {
        requests.map { request ->
            _result.addSource(request.result()) {
                globalRequest.newEvent(request.status())
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

    fun clear() {
        requests.map {
            _result.removeSource(it.result())
        }
    }

    override fun execute(params: Any) {
        requests.map {
            it.retry()
        }
    }

    override fun result(): LiveData<PromiseTask<Any, Any>?> {
        return result
    }

}
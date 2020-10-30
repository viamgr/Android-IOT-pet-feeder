package com.viam.feeder.core.task

import androidx.hilt.Assisted
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import com.viam.feeder.core.Resource
import com.viam.feeder.core.isError
import kotlinx.coroutines.*
import javax.inject.Inject
import kotlin.coroutines.CoroutineContext

@Suppress("UNCHECKED_CAST")
class CompositeRequest @Inject constructor(
    private val globalRequest: GlobalRequest,
    @Assisted val coroutineContext: CoroutineContext,
    @Assisted vararg val requests: Request<*, *>
) {

    private var currentJob: Job? = null

    private val _result = MediatorLiveData<Resource<Unit>>()
    val result: LiveData<Resource<Unit>> = _result

    init {
        requests.map { request ->
            _result.addSource(request.result) { res: Resource<Any?>? ->
                res?.let {
                    globalRequest.newEvent(it)
                }
                val notSuccessList = requests
                    .filterNot { it.result.value == null || it.result.value is Resource.Success }
                    .map { it.result.value }
                if (notSuccessList.isEmpty()) {
                    _result.value = Resource.Success(Unit)
                } else {
                    if (!notSuccessList.any { it is Resource.Error }) {
                        _result.value = Resource.Loading
                    } else {
                        val errors = notSuccessList.filterIsInstance(Resource.Error::class.java)
                            .map { it.exception }
                        _result.value = Resource.Error(CompositeException(errors))
                    }
                }
            }
        }
    }

    fun retry() {
        currentJob = CoroutineScope(coroutineContext).launch {
            requests.map { request: Request<*, *> ->
                async {
                    if (request.result.value?.isError() == true) {
                        request.retry()
                    }
                }
            }.awaitAll()
        }
    }

    fun cancel() {
        currentJob?.cancel()
        for (index in requests.indices) {
            val request = requests[index]
            request.cancel()
        }
    }

    fun clear() {
        for (i in requests.indices) {
            val request = requests[i]
            _result.removeSource(request.result)
        }
    }

    fun launch(block: suspend () -> Unit) {
        currentJob = CoroutineScope(coroutineContext).launch {
            block()
        }
    }

}
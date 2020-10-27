package com.viam.feeder.data.repository

import androidx.lifecycle.MediatorLiveData
import com.viam.feeder.core.Resource
import com.viam.feeder.core.isError
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext

class CombinedRequest<T>(
    private val coroutineContext: CoroutineContext,
    private vararg val requests: Request<*, *>
) {
    val resource = MediatorLiveData<Resource<T>>()
    private var currentJob: Job? = null

    init {
        for (i in requests.indices) {
            resource.addSource(requests[i].result) {
                resource.value = it as Resource<T>?
            }
        }
    }

    fun retry() {
        currentJob = CoroutineScope(coroutineContext).launch {
            for (index in requests.indices) {
                val request = requests[index]
                if (request.result.value?.isError() == true) {
                    request.retry()
                }
            }
        }
    }

    fun clear() {
        currentJob?.cancel()
        for (index in requests.indices) {
            val request = requests[index]
            request.clear()
        }
    }

    fun call(block: suspend () -> Unit) {
        currentJob = CoroutineScope(coroutineContext).launch {
            block()
        }
    }

}
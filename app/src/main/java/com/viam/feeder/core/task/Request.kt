package com.viam.feeder.core.task

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.viam.feeder.core.Resource
import java.util.concurrent.CancellationException

data class Request<R, T>(
    val result: LiveData<Resource<T>?>,
    val retry: suspend () -> Unit,
    val cancel: () -> Unit,
    val execute: suspend (requestBody: R) -> Unit
)

fun <R, T : Any> makeRequest(requestBlock: suspend (R) -> Resource<T>): Request<R, T> {
    val result = MutableLiveData<Resource<T>?>()
    var retryRequest: (suspend () -> Unit)? = null
    var execute: (suspend (R) -> Unit)? = null
    execute = { requestBody: R ->
        result.postValue(Resource.Loading)
        val output = requestBlock(requestBody)
        if (output is Resource.Error) {
            retryRequest = {
                execute?.invoke(requestBody)
            }
        }
        if (output is Resource.Error && output.exception !is CancellationException) {
            result.postValue(output)
        }

    }
    return Request(result = result, retry = {
        retryRequest?.invoke()
    }, cancel = {
        result.postValue(null)
    }, execute = execute)
}

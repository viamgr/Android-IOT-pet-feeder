package com.viam.feeder.data.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.viam.feeder.core.Resource
import com.viam.feeder.data.datasource.UploadDataSource
import dagger.hilt.android.scopes.ActivityScoped
import okhttp3.MultipartBody.Part
import java.util.concurrent.CancellationException
import javax.inject.Inject

@ActivityScoped
class UploadRepository @Inject constructor(private val uploadDataSource: UploadDataSource) {
    suspend fun uploadSound(body: Part) = uploadDataSource.uploadEating(body)
}

fun <R, T : Any> makeRequest(requestBlock: suspend (R) -> Resource<T>): Request<R, T> {
    val result = MutableLiveData<Resource<T>?>()
    var retryRequest: (suspend () -> Unit)? = null
    var call: (suspend (R) -> Unit)? = null
    call = { requestBody: R ->
        result.postValue(Resource.Loading)
        val output = requestBlock(requestBody)
        if (output is Resource.Error) {
            retryRequest = {
                call?.invoke(requestBody)
            }
        }
        if (output !is Resource.Error || output.exception !is CancellationException)
            result.postValue(output)
    }
    return Request(result = result, retry = {
        retryRequest?.invoke()
    }, clear = {
        result.postValue(null)
    }, call = call)
}

data class Request<R, T>(
    val result: LiveData<Resource<T>?>,
    val retry: suspend () -> Unit,
    val clear: () -> Unit,
    val call: suspend (requestBody: R) -> Unit
)
package com.viam.feeder.core.network

import com.viam.feeder.core.Resource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

suspend inline fun <T : Any> safeApiCall(
    crossinline body: suspend () -> T
): Resource<T> {
    return try {
        Resource.Success(body())
    } catch (e: Exception) {
        e.printStackTrace()
        withContext(Dispatchers.Main) {
        }
        Resource.Error(e)
    }
}
package com.viam.feeder.core.network

import android.widget.Toast
import com.viam.feeder.MyApplication
import com.viam.feeder.core.Resource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

suspend inline fun <T : Any> safeApiCall(
    crossinline body: suspend () -> T
): Resource<T> {
    return try {
        Resource.Success(body())
    } catch (e: Exception) {
        withContext(Dispatchers.Main) {
            Toast.makeText(MyApplication.context, e.message, Toast.LENGTH_SHORT).show()
        }
        Resource.Error(e)
    }
}
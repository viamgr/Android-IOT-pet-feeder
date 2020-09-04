/*
 * Copyright 2018 Google LLC.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.viam.feeder.core.network

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.viam.feeder.core.Resource
import com.viam.feeder.core.onError
import com.viam.feeder.core.onSuccess
import com.viam.feeder.core.utility.postValueIfChanged
import com.viam.feeder.services.GlobalConfigRepository
import dagger.hilt.android.scopes.ActivityScoped
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import javax.inject.Inject

@ActivityScoped
class NetworkStatus @Inject constructor(
    private val globalConfigRepository: GlobalConfigRepository,
    private val dispatcherProvider: CoroutinesDispatcherProvider
) {
    private val _connection = MutableLiveData<Int>(CONNECTION_STATE_CONNECTING)
    val connection: LiveData<Int> = _connection

    suspend fun check() {
        withContext(dispatcherProvider.io) {
            safeApiCall {
                globalConfigRepository.getStatus()
            }.onSuccess {
                _connection.postValueIfChanged(CONNECTION_STATE_SUCCESS)
            }.onError {
                if (it !is HttpException || it.code() == 404) {
                    _connection.postValueIfChanged(CONNECTION_STATE_WRONG)
                }
            }
            delay(10000)
            check()
        }
    }

    companion object {
        const val CONNECTION_STATE_CONNECTING = 0
        const val CONNECTION_STATE_WRONG = 2
        const val CONNECTION_STATE_SUCCESS = 3
    }

    suspend fun runIfConnected(block: suspend () -> Unit) {
        if (connection.value == CONNECTION_STATE_SUCCESS) {
            block()
        }
    }

}


suspend inline fun <T : Any> safeApiCall(
    crossinline body: suspend () -> T
): Resource<T> {
    return try {
        // blocking block
        val users = withContext(Dispatchers.IO) {
            body()
        }
        Resource.Success(users)
    } catch (e: Exception) {
        Resource.Error(e)
    }
}
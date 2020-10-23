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

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkInfo
import android.net.wifi.WifiManager
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.viam.feeder.core.onError
import com.viam.feeder.core.onSuccess
import com.viam.feeder.core.utility.postValueIfChanged
import com.viam.feeder.data.repository.GlobalConfigRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.android.scopes.ActivityScoped
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import javax.inject.Inject


@Suppress("DEPRECATION")
@ActivityScoped
class NetworkStatus @Inject constructor(
    private val globalConfigRepository: GlobalConfigRepository,
    private val dispatcherProvider: CoroutinesDispatcherProvider,
    @ApplicationContext private val appContext: Context
) {
    var isShowing = false
    private val _connection = MutableLiveData<Int>(CONNECTION_STATE_CONNECTING)
    val connection: LiveData<Int> = _connection

    private val _wifiEnabled = MutableLiveData<Boolean>()
    val wifiEnabled: LiveData<Boolean> = _wifiEnabled

    private fun setWifiStatus(enabled: Boolean) {
        _wifiEnabled.postValue(enabled)
    }

    var wifi: WifiManager? =
        appContext.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager?

    private val cm =
        appContext.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    private val activeNetwork: NetworkInfo? = cm.activeNetworkInfo

    suspend fun check() {
        withContext(dispatcherProvider.io) {
            val enabled = activeNetwork?.isConnectedOrConnecting == true
            setWifiStatus(enabled)
            if (enabled) {
                safeApiCall {
                    globalConfigRepository.getStatus()
                }.onSuccess {
                    _connection.postValueIfChanged(CONNECTION_STATE_SUCCESS)
                }.onError {
                    if (it !is HttpException || it.code() == 404) {
                        _connection.postValue(CONNECTION_STATE_WRONG)
                    }
                }
            }
            delay(300000)
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
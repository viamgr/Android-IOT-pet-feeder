package com.viam.feeder.data.domain.config

import com.viam.feeder.core.network.CoroutinesDispatcherProvider
import com.viam.feeder.data.storage.ConfigFields
import com.viam.feeder.data.storage.JsonPreferences
import com.viam.feeder.socket.WebSocketApi
import dagger.hilt.android.scopes.ActivityScoped
import javax.inject.Inject

@ActivityScoped
class SetWifiCredentials @Inject constructor(
    coroutinesDispatcherProvider: CoroutinesDispatcherProvider,
    private val configFields: ConfigFields,
    jsonPreferences: JsonPreferences,
    webSocketApi: WebSocketApi,
) : BaseSetConfig<WifiAuthentication>(
    coroutinesDispatcherProvider.io,
    webSocketApi,
    jsonPreferences
) {
    override suspend fun setConfigField(value: WifiAuthentication) {
        configFields.wifiSsid.store(value.ssid)
        configFields.wifiPassword.store(value.password)
    }
}

data class WifiAuthentication(val ssid: String, val password: String)
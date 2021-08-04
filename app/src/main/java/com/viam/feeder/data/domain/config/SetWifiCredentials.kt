package com.viam.feeder.data.domain.config

import com.viam.feeder.core.network.CoroutinesDispatcherProvider
import com.viam.feeder.data.storage.ConfigFields
import com.viam.feeder.data.storage.JsonPreferences
import javax.inject.Inject


class SetWifiCredentials @Inject constructor(
    coroutinesDispatcherProvider: CoroutinesDispatcherProvider,
    private val configFields: ConfigFields,
    jsonPreferences: JsonPreferences,
    webSocketApi: com.viam.websocket.WebSocketApi,
) : BaseSetConfig<WifiAuthentication>(
    coroutinesDispatcherProvider,
    webSocketApi,
    jsonPreferences
) {
    override suspend fun setConfigField(value: WifiAuthentication) {
        configFields.wifiSsid.store(value.ssid)
        configFields.wifiPassword.store(value.password)
    }
}

data class WifiAuthentication(val ssid: String, val password: String)
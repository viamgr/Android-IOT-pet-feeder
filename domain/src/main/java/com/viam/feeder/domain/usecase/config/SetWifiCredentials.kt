package com.viam.feeder.domain.usecase.config

import com.viam.feeder.domain.base.CoroutinesDispatcherProvider
import com.viam.feeder.domain.repositories.socket.WebSocketRepository
import com.viam.feeder.domain.repositories.system.ConfigFields
import com.viam.feeder.domain.repositories.system.JsonPreferences
import com.viam.feeder.shared.FeederConstants.WifiMode.WIFI_MODE_AP_STA
import javax.inject.Inject

class SetWifiCredentials @Inject constructor(
    coroutinesDispatcherProvider: CoroutinesDispatcherProvider,
    private val configFields: ConfigFields,
    jsonPreferences: JsonPreferences,
    webSocketRepository: WebSocketRepository,
) : BaseSetConfig<WifiAuthentication>(
    coroutinesDispatcherProvider,
    webSocketRepository,
    jsonPreferences
) {
    override suspend fun setConfigField(value: WifiAuthentication) {
        configFields.setWifiSsid(value.ssid)
        configFields.setWifiPassword(value.password)
        configFields.setWifiMode(WIFI_MODE_AP_STA)
    }
}

data class WifiAuthentication(
    val ssid: String,
    val password: String,
    val ip: String? = null,
    val gateway: String? = null,
    val port: Int? = null
)
package com.viam.feeder.domain.usecase.config

import com.viam.feeder.domain.base.CoroutinesDispatcherProvider
import com.viam.feeder.domain.repositories.socket.WebSocketRepository
import com.viam.feeder.domain.repositories.system.ConfigFields
import com.viam.feeder.domain.repositories.system.JsonPreferences
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
    }
}

data class WifiAuthentication(val ssid: String, val password: String)
package com.viam.feeder.domain.usecase.config

import com.viam.feeder.domain.base.CoroutinesDispatcherProvider
import com.viam.feeder.domain.repositories.socket.WebSocketRepository
import com.viam.feeder.domain.repositories.system.ConfigFields
import com.viam.feeder.domain.repositories.system.JsonPreferences
import com.viam.feeder.shared.FeederConstants.WifiMode.WIFI_MODE_STA
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
        configFields.setWifiMode(WIFI_MODE_STA)
        val useDhcp =
            if (value.useStatic == false || value.staticIp.isNullOrEmpty() || value.staticIp.isNullOrEmpty() || value.subnet.isNullOrEmpty()) {
                1
            } else 0

        configFields.setUseDhcp(useDhcp)
        configFields.setGateway(value.gateway.orEmpty())
        configFields.setStaticIp(value.staticIp.orEmpty())
        configFields.setSubnet(value.subnet.orEmpty())
    }
}

data class WifiAuthentication(
    val ssid: String,
    val password: String,
    val staticIp: String? = null,
    val gateway: String? = null,
    val subnet: String? = null,
    val port: Int? = null,
    val useStatic: Boolean? = null
)
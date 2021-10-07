package com.viam.feeder.domain.usecase.config

import com.viam.feeder.domain.base.CoroutinesDispatcherProvider
import com.viam.feeder.domain.repositories.socket.WebSocketRepository
import com.viam.feeder.domain.repositories.system.ConfigFields
import com.viam.feeder.domain.repositories.system.JsonPreferences
import com.viam.feeder.shared.FeederConstants.WifiMode
import javax.inject.Inject

class SetWifiMode @Inject constructor(
    coroutinesDispatcherProvider: CoroutinesDispatcherProvider,
    private val configFields: ConfigFields,
    jsonPreferences: JsonPreferences,
    webSocketRepository: WebSocketRepository,
) : BaseSetConfig<WifiMode>(
    coroutinesDispatcherProvider,
    webSocketRepository,
    jsonPreferences
) {
    override suspend fun setConfigField(value: WifiMode) {
        configFields.setWifiMode(value)
    }
}
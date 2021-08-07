package com.viam.feeder.domain.usecase.config

import com.viam.feeder.domain.base.CoroutinesDispatcherProvider
import com.viam.feeder.domain.repositories.socket.WebSocketRepository
import com.viam.feeder.domain.repositories.system.ConfigFields
import com.viam.feeder.domain.repositories.system.JsonPreferences
import javax.inject.Inject


class SetFeedingDuration @Inject constructor(
    coroutinesDispatcherProvider: CoroutinesDispatcherProvider,
    private val configFields: ConfigFields,
    webSocketRepository: WebSocketRepository,
    jsonPreferences: JsonPreferences,
) : BaseSetConfig<Int>(coroutinesDispatcherProvider, webSocketRepository, jsonPreferences) {
    override suspend fun setConfigField(value: Int) {
        configFields.setFeedingDuration(value)
    }
}
package com.viam.feeder.data.domain.config

import com.viam.feeder.core.network.CoroutinesDispatcherProvider
import com.viam.feeder.data.storage.ConfigFields
import com.viam.feeder.data.storage.JsonPreferences
import com.viam.websocket.WebSocketApi
import dagger.hilt.android.scopes.ActivityScoped
import javax.inject.Inject

@ActivityScoped
class SetLedState @Inject constructor(
    coroutinesDispatcherProvider: CoroutinesDispatcherProvider,
    private val configFields: ConfigFields,
    webSocketApi: WebSocketApi,
    jsonPreferences: JsonPreferences,
) : BaseSetConfig<Int>(coroutinesDispatcherProvider, webSocketApi, jsonPreferences) {
    override suspend fun setConfigField(value: Int) {
        configFields.ledState.store(value)
    }
}
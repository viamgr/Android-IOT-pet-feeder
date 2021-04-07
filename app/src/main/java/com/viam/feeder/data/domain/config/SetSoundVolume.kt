package com.viam.feeder.data.domain.config

import com.viam.feeder.core.network.CoroutinesDispatcherProvider
import com.viam.feeder.data.storage.ConfigFields
import com.viam.feeder.data.storage.JsonPreferences
import com.viam.websocket.WebSocketApi
import dagger.hilt.android.scopes.ActivityScoped
import javax.inject.Inject

@ActivityScoped
class SetSoundVolume @Inject constructor(
    coroutinesDispatcherProvider: CoroutinesDispatcherProvider,
    private val configFields: ConfigFields,
    webSocketApi: WebSocketApi,
    jsonPreferences: JsonPreferences,
) : BaseSetConfig<Float>(coroutinesDispatcherProvider, webSocketApi, jsonPreferences) {
    override suspend fun setConfigField(value: Float) {
        configFields.soundVolume.store(value)
    }
}
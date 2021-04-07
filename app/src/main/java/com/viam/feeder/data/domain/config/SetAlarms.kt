package com.viam.feeder.data.domain.config

import com.viam.feeder.core.network.CoroutinesDispatcherProvider
import com.viam.feeder.data.models.ClockTimer
import com.viam.feeder.data.storage.ConfigFields
import com.viam.feeder.data.storage.JsonPreferences
import dagger.hilt.android.scopes.ActivityScoped
import javax.inject.Inject

@ActivityScoped
class SetAlarms @Inject constructor(
    coroutinesDispatcherProvider: CoroutinesDispatcherProvider,
    private val configFields: ConfigFields,
    webSocketApi: com.viam.websocket.WebSocketApi,
    jsonPreferences: JsonPreferences
) : BaseSetConfig<List<ClockTimer>>(
    coroutinesDispatcherProvider,
    webSocketApi,
    jsonPreferences
) {
    override suspend fun setConfigField(value: List<ClockTimer>) {
        configFields.alarms.store(value.map {
            String.format("0 %d %d * * *", it.minute, it.hour)
        })
    }

}
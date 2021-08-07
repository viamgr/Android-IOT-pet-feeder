package com.viam.feeder.domain.usecase.config

import com.viam.feeder.domain.base.CoroutinesDispatcherProvider
import com.viam.feeder.domain.repositories.socket.WebSocketRepository
import com.viam.feeder.domain.repositories.system.ConfigFields
import com.viam.feeder.domain.repositories.system.JsonPreferences
import com.viam.feeder.model.ClockTimer
import javax.inject.Inject


class SetAlarms @Inject constructor(
    coroutinesDispatcherProvider: CoroutinesDispatcherProvider,
    private val configFields: ConfigFields,
    WebSocketRepository: WebSocketRepository,
    jsonPreferences: JsonPreferences
) : BaseSetConfig<List<ClockTimer>>(
    coroutinesDispatcherProvider,
    WebSocketRepository,
    jsonPreferences
) {
    override suspend fun setConfigField(value: List<ClockTimer>) {
        configFields.setAlarms(value.map {
            String.format("0 %d %d * * *", it.minute, it.hour)
        })
    }

}
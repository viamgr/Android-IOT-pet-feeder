package com.viam.feeder.data.domain.config

import com.viam.feeder.core.network.CoroutinesDispatcherProvider
import com.viam.feeder.data.models.ClockTimer
import com.viam.feeder.data.storage.ConfigStorage
import dagger.hilt.android.scopes.ActivityScoped
import javax.inject.Inject

@ActivityScoped
class GetAlarms @Inject constructor(
    coroutinesDispatcherProvider: CoroutinesDispatcherProvider,
    private val configStorage: ConfigStorage,
) : BaseGetConfig<List<ClockTimer>>(coroutinesDispatcherProvider.io, configStorage) {
    override suspend fun getField() = configStorage.alarms.mapIndexed { index, value ->
        val splitCron = value.split(" ")
        ClockTimer(id = index, hour = splitCron[2].toInt(), minute = splitCron[1].toInt())
    }
}
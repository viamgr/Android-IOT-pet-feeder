package com.viam.feeder.data.domain.config

import com.viam.feeder.core.network.CoroutinesDispatcherProvider
import com.viam.feeder.data.models.ClockTimer
import com.viam.feeder.data.repository.ConfigsRepository
import com.viam.feeder.data.storage.ConfigStorageImpl
import dagger.hilt.android.scopes.ActivityScoped
import javax.inject.Inject

@ActivityScoped
class GetAlarms @Inject constructor(
    coroutinesDispatcherProvider: CoroutinesDispatcherProvider,
    private val configStorage: ConfigStorageImpl,
    configsRepository: ConfigsRepository,
) : BaseGetConfig<List<ClockTimer>>(
    coroutinesDispatcherProvider.io,
    configStorage,
    configsRepository
) {
    override suspend fun getField() = configStorage.alarms.mapIndexed { index, value ->
        val splitCron = value.split(" ")
        ClockTimer(id = index, hour = splitCron[2].toInt(), minute = splitCron[1].toInt())
    }
}
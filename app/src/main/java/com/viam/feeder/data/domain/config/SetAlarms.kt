package com.viam.feeder.data.domain.config

import com.viam.feeder.core.network.CoroutinesDispatcherProvider
import com.viam.feeder.data.models.ClockTimer
import com.viam.feeder.data.repository.ConfigsRepository
import com.viam.feeder.data.storage.ConfigStorageImpl
import dagger.hilt.android.scopes.ActivityScoped
import javax.inject.Inject

@ActivityScoped
class SetAlarms @Inject constructor(
    coroutinesDispatcherProvider: CoroutinesDispatcherProvider,
    private val configStorage: ConfigStorageImpl,
    configsRepository: ConfigsRepository,
) : BaseSetConfig<List<ClockTimer>>(
    coroutinesDispatcherProvider.io,
    configStorage,
    configsRepository
) {
    override suspend fun setConfigField(value: List<ClockTimer>) {
        configStorage.alarms = value.map {
            String.format("0 %d %d * * *", it.minute, it.hour)
        }
    }

}
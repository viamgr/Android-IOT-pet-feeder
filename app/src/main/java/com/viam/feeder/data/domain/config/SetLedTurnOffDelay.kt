package com.viam.feeder.data.domain.config

import com.viam.feeder.core.network.CoroutinesDispatcherProvider
import com.viam.feeder.data.repository.ConfigsRepository
import com.viam.feeder.data.storage.ConfigStorageImpl
import dagger.hilt.android.scopes.ActivityScoped
import javax.inject.Inject

@ActivityScoped
class SetLedTurnOffDelay @Inject constructor(
    coroutinesDispatcherProvider: CoroutinesDispatcherProvider,
    private val configStorage: ConfigStorageImpl,
    configsRepository: ConfigsRepository,
) : BaseSetConfig<Int>(coroutinesDispatcherProvider.io, configStorage, configsRepository) {
    override suspend fun setConfigField(value: Int) {
        configStorage.ledTurnOffDelay = value
    }
}
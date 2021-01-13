package com.viam.feeder.data.domain.config

import com.viam.feeder.core.network.CoroutinesDispatcherProvider
import com.viam.feeder.data.repository.ConfigsRepository
import com.viam.feeder.data.storage.ConfigStorageImpl
import dagger.hilt.android.scopes.ActivityScoped
import javax.inject.Inject

@ActivityScoped
class GetLedTurnOffDelay @Inject constructor(
    coroutinesDispatcherProvider: CoroutinesDispatcherProvider,
    private val configStorage: ConfigStorageImpl,
    configsRepository: ConfigsRepository,
) : BaseGetConfig<Int>(coroutinesDispatcherProvider.io, configStorage, configsRepository) {
    override suspend fun getField() = configStorage.ledTurnOffDelay
}
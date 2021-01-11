package com.viam.feeder.data.domain.config

import com.viam.feeder.core.network.CoroutinesDispatcherProvider
import com.viam.feeder.data.repository.ConfigsRepository
import com.viam.feeder.data.storage.ConfigStorage
import dagger.hilt.android.scopes.ActivityScoped
import javax.inject.Inject

@ActivityScoped
class GetSoundVolume @Inject constructor(
    coroutinesDispatcherProvider: CoroutinesDispatcherProvider,
    private val configStorage: ConfigStorage,
    configsRepository: ConfigsRepository,
) : BaseGetConfig<Float>(coroutinesDispatcherProvider.io, configStorage, configsRepository) {
    override suspend fun getField() = configStorage.soundVolume
}
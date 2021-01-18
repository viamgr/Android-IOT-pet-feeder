package com.viam.feeder.data.domain.config

import com.viam.feeder.core.network.CoroutinesDispatcherProvider
import com.viam.feeder.data.repository.UploadRepository
import com.viam.feeder.data.storage.ConfigStorage
import dagger.hilt.android.scopes.ActivityScoped
import javax.inject.Inject

@ActivityScoped
class SetSoundVolume @Inject constructor(
    coroutinesDispatcherProvider: CoroutinesDispatcherProvider,
    private val configStorage: ConfigStorage,
    uploadRepository: UploadRepository,
) : BaseSetConfig<Float>(coroutinesDispatcherProvider.io, configStorage, uploadRepository) {
    override suspend fun setConfigField(value: Float) {
        configStorage.soundVolume = value
    }
}
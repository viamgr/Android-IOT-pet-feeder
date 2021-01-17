package com.viam.feeder.data.domain.config

import com.viam.feeder.core.network.CoroutinesDispatcherProvider
import com.viam.feeder.data.repository.UploadRepository
import com.viam.feeder.data.storage.ConfigStorageImpl
import dagger.hilt.android.scopes.ActivityScoped
import javax.inject.Inject

@ActivityScoped
class SetLedTurnOffDelay @Inject constructor(
    coroutinesDispatcherProvider: CoroutinesDispatcherProvider,
    private val configStorage: ConfigStorageImpl,
    uploadRepository: UploadRepository,
) : BaseSetConfig<Int>(coroutinesDispatcherProvider.io, configStorage, uploadRepository) {
    override suspend fun setConfigField(value: Int) {
        configStorage.ledTurnOffDelay = value
    }
}
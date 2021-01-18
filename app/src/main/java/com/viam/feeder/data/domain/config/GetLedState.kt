package com.viam.feeder.data.domain.config

import com.viam.feeder.core.network.CoroutinesDispatcherProvider
import com.viam.feeder.data.storage.ConfigStorage
import dagger.hilt.android.scopes.ActivityScoped
import javax.inject.Inject

@ActivityScoped
class GetLedState @Inject constructor(
    coroutinesDispatcherProvider: CoroutinesDispatcherProvider,
    private val configStorage: ConfigStorage
) : BaseGetConfig<Int>(coroutinesDispatcherProvider.io, configStorage) {
    override suspend fun getField() = configStorage.ledState
}
package com.viam.feeder.data.domain.config

import com.viam.feeder.core.domain.UseCase
import com.viam.feeder.data.repository.ConfigsRepository
import com.viam.feeder.data.storage.ConfigStorage
import kotlinx.coroutines.CoroutineDispatcher

abstract class BaseGetConfig<T>(
    coroutineDispatcher: CoroutineDispatcher,
    private val configStorage: ConfigStorage,
    private val configsRepository: ConfigsRepository,
) : UseCase<Unit, T>(coroutineDispatcher) {
    override suspend fun execute(parameters: Unit): T {
        if (!configStorage.isConfigured()) {
            downloadConfigs()
        }
        return getField()
    }

    @Throws(RuntimeException::class)
    protected abstract suspend fun getField(): T

    private suspend fun downloadConfigs() {
        configStorage.write(configsRepository.downloadConfigs())
    }
}
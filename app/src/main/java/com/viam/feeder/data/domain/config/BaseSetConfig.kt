package com.viam.feeder.data.domain.config

import com.viam.feeder.core.domain.UseCase
import com.viam.feeder.data.repository.ConfigsRepository
import com.viam.feeder.data.storage.ConfigStorage
import kotlinx.coroutines.CoroutineDispatcher
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody

abstract class BaseSetConfig<T>(
    coroutineDispatcher: CoroutineDispatcher,
    private val configStorage: ConfigStorage,
    private val configsRepository: ConfigsRepository,
) : UseCase<T, Unit>(coroutineDispatcher) {
    override suspend fun execute(parameters: T) {
        setConfigField(parameters)
        if (configStorage.isConfigured()) {
            uploadConfigs()
        } else {
            throw Exception("Configs should be set.")
        }
    }

    @Throws(RuntimeException::class)
    protected abstract suspend fun setConfigField(value: T)

    private suspend fun uploadConfigs() {
        configStorage.save()
        val requestFile: RequestBody =
            configStorage.file.asRequestBody("multipart/form-data".toMediaTypeOrNull())
        val body = MultipartBody.Part.createFormData("Config", "config.json", requestFile)
        configsRepository.uploadConfigs(body)
    }
}
package com.viam.feeder.data.domain.config

import androidx.annotation.CallSuper
import com.viam.feeder.core.domain.UseCase
import com.viam.feeder.data.repository.UploadRepository
import com.viam.feeder.data.storage.ConfigStorage
import kotlinx.coroutines.CoroutineDispatcher
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody

abstract class BaseSetConfig<T>(
    coroutineDispatcher: CoroutineDispatcher,
    private val configStorage: ConfigStorage,
    private val uploadRepository: UploadRepository,
) : UseCase<T, Unit>(coroutineDispatcher) {

    @CallSuper
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
        val requestFile: RequestBody =
            configStorage.getJsonString()
                .toRequestBody("application/octet-stream".toMediaTypeOrNull())
        val body = MultipartBody.Part.createFormData("data", "config.json", requestFile)
        uploadRepository.uploadFile(body)
    }
}
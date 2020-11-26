package com.viam.feeder.data.domain.specification

import com.viam.feeder.core.domain.UseCase
import com.viam.feeder.core.network.CoroutinesDispatcherProvider
import com.viam.feeder.data.repository.ConvertRepository
import com.viam.feeder.data.repository.UploadRepository
import dagger.hilt.android.scopes.ActivityScoped
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import javax.inject.Inject

@ActivityScoped
class ConvertUploadSound @Inject constructor(
    coroutinesDispatcherProvider: CoroutinesDispatcherProvider,
    private val convertRepository: ConvertRepository,
    private val uploadRepository: UploadRepository
) :
    UseCase<Pair<String, String>, Unit>(coroutinesDispatcherProvider.io) {
    override suspend fun execute(parameters: Pair<String, String>) {
        val file = convertRepository.convertToMp3(parameters.first, parameters.second)
        val requestFile: RequestBody =
            file.asRequestBody("multipart/form-data".toMediaTypeOrNull())
        val body = MultipartBody.Part.createFormData("filename", file.name, requestFile)
        uploadRepository.uploadSound(body)
    }

}
package com.viam.feeder.data.domain.specification

import android.content.Context
import com.viam.feeder.core.domain.UseCase
import com.viam.feeder.core.network.CoroutinesDispatcherProvider
import com.viam.feeder.data.repository.UploadRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.android.scopes.ActivityScoped
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import javax.inject.Inject

@ActivityScoped
class UploadSound @Inject constructor(
    coroutinesDispatcherProvider: CoroutinesDispatcherProvider,
    private val uploadRepository: UploadRepository,
    @ApplicationContext private val appContext: Context
) : UseCase<Int, Unit>(coroutinesDispatcherProvider.io) {
    override suspend fun execute(parameters: Int) {
        val inputStream = appContext.resources.openRawResource(parameters)
        val body = MultipartBody.Part.createFormData(
            "filename", "$parameters", inputStream.readBytes().toRequestBody()
        )
        uploadRepository.uploadSound(body)
    }

}
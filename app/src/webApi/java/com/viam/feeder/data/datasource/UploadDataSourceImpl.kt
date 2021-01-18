package com.viam.feeder.data.datasource

import com.viam.feeder.data.api.UploadService
import okhttp3.MultipartBody
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UploadDataSourceImpl @Inject constructor(private val uploadService: UploadService) :
    UploadDataSource {
    override suspend fun upload(body: MultipartBody.Part) {
        uploadService.uploadEating(body)
    }
}
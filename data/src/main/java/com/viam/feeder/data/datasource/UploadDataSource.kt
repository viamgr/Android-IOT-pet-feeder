package com.viam.feeder.data.datasource

import okhttp3.MultipartBody

interface UploadDataSource {
    suspend fun upload(body: MultipartBody.Part)
}
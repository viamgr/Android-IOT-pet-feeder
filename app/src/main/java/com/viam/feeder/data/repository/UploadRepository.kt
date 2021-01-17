package com.viam.feeder.data.repository

import com.viam.feeder.data.datasource.UploadDataSource
import dagger.hilt.android.scopes.ActivityScoped
import okhttp3.MultipartBody.Part
import javax.inject.Inject

@ActivityScoped
class UploadRepository @Inject constructor(private val uploadDataSource: UploadDataSource) {
    suspend fun uploadFile(body: Part) = uploadDataSource.upload(body)
}
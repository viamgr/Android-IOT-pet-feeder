package com.viam.feeder.data.datasource

import com.viam.feeder.data.remote.UploadService
import dagger.hilt.android.scopes.ActivityScoped
import okhttp3.MultipartBody
import javax.inject.Inject

@ActivityScoped
class UploadDataSource @Inject constructor(private val uploadService: UploadService) {

    suspend fun uploadEating(body: MultipartBody.Part) {
        uploadService.uploadEating(body)
    }

}
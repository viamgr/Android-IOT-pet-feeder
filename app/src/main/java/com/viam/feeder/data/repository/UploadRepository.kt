package com.viam.feeder.data.repository

import com.viam.feeder.core.Resource
import com.viam.feeder.core.network.safeApiCall
import com.viam.feeder.data.datasource.UploadDataSource
import dagger.hilt.android.scopes.ActivityScoped
import okhttp3.MultipartBody
import javax.inject.Inject

@ActivityScoped
class UploadRepository @Inject constructor(private val uploadDataSource: UploadDataSource) {
    suspend fun uploadEating(body: MultipartBody.Part): Resource<Unit> {
        return safeApiCall {
            uploadDataSource.uploadEating(body)
        }
    }

}
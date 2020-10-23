package com.viam.feeder.data.datasource

import kotlinx.coroutines.delay
import okhttp3.MultipartBody
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UploadDataSource @Inject constructor() {

    suspend fun uploadEating(body: MultipartBody.Part) {
        delay(3000)
    }

}
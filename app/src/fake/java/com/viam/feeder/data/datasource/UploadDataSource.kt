package com.viam.feeder.data.datasource

import kotlinx.coroutines.delay
import okhttp3.MultipartBody
import javax.inject.Inject

@ActivityScoped
class UploadDataSource @Inject constructor() {

    suspend fun uploadEating(body: MultipartBody.Part) {
        delay(3000)
    }

}
package com.viam.feeder.data.datasource

import com.viam.feeder.data.utils.randomException
import dagger.hilt.android.scopes.ActivityScoped
import okhttp3.MultipartBody
import javax.inject.Inject

@ActivityScoped
class UploadDataSource @Inject constructor() {

    suspend fun uploadEating(body: MultipartBody.Part) = randomException {

    }

}
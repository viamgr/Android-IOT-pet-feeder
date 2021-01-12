package com.viam.feeder.data.datasource

import com.viam.feeder.data.utils.fakeRequest
import dagger.hilt.android.scopes.ActivityScoped
import okhttp3.MultipartBody
import javax.inject.Inject

@ActivityScoped
class ConfigsDataSource @Inject constructor() {

    suspend fun downloadConfigs() = fakeRequest {
        "{\"soundVolume\":3.99}".byteInputStream()
    }

    suspend fun uploadConfigs(configs: MultipartBody.Part) = fakeRequest {

    }

}
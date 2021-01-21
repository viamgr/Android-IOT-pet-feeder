package com.viam.feeder.data.datasource

import com.viam.feeder.data.utils.fakeRequest
import com.viam.feeder.ui.wifi.NetworkStatusObserver
import okhttp3.MultipartBody
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UploadDataSourceImpl @Inject constructor(private val networkStatusObserver: NetworkStatusObserver) :
    UploadDataSource {
    override suspend fun upload(body: MultipartBody.Part) = fakeRequest(networkStatusObserver) {

    }
}
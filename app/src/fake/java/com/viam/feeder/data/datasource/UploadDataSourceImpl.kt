package com.viam.feeder.data.datasource

import android.content.Context
import com.viam.feeder.data.utils.fakeRequest
import dagger.hilt.android.qualifiers.ApplicationContext
import okhttp3.MultipartBody
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UploadDataSourceImpl @Inject constructor(@ApplicationContext private val context: Context) :
    UploadDataSource {
    override suspend fun upload(body: MultipartBody.Part) = fakeRequest(context) {

    }
}
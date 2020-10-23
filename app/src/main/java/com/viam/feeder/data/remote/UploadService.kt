package com.viam.feeder.data.remote

import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

interface UploadService {
    @Multipart
    @POST("upload/")
    suspend fun uploadEating(@Part image: MultipartBody.Part): Response<Unit>
}
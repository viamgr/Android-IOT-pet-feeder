package com.viam.feeder.data.api

import okhttp3.MultipartBody
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

interface ConfigsService {
    @GET("/upload/config.json")
    suspend fun downloadConfigs(): Response<ResponseBody>

    @Multipart
    @POST("upload/")
    suspend fun uploadConfigs(@Part configs: MultipartBody.Part): Response<Unit>
}
package com.viam.feeder.data.api

import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.GET

interface ConfigsService {
    @GET("config.json")
    suspend fun downloadConfigs(): Response<ResponseBody>

}
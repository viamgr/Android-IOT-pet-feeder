package com.viam.feeder.data.api

import retrofit2.http.GET
import retrofit2.http.POST

interface WifiService {

    @GET("wifi/list")
    suspend fun list(): List<com.viam.feeder.model.WifiDevice>

    @POST("wifi/connect")
    suspend fun connect(ssid: String, password: String)

}
package com.viam.feeder.data.api

import com.viam.feeder.data.models.WifiDevice
import retrofit2.http.GET
import retrofit2.http.POST

interface WifiService {

    @GET("wifi/list")
    suspend fun list(): List<WifiDevice>

    @POST("wifi/connect")
    suspend fun connect(ssid: String, password: String)

}
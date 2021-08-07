package com.viam.feeder.data.api

import retrofit2.http.GET
import retrofit2.http.Query

interface EventService {

    @GET("events")
    suspend fun send(@Query("name") name: String)

    @GET("setStatus")
    suspend fun setStatus(@Query("key") key: String, @Query("value") value: String)

    @GET("getStatus")
    suspend fun getStatus(@Query("key") key: String): String

}
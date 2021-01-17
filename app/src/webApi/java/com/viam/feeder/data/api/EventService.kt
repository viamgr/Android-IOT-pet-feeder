package com.viam.feeder.data.api

import retrofit2.http.GET
import retrofit2.http.Query

interface EventService {

    @GET("events")
    suspend fun send(@Query("name") name: String)

}
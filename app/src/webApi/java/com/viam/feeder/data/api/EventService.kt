package com.viam.feeder.data.api

import com.viam.feeder.data.models.KeyValue
import retrofit2.http.Body
import retrofit2.http.POST

interface EventService {

    @POST("events/")
    suspend fun save(@Body event: KeyValue)

}
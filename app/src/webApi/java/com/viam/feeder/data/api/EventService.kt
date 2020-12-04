package com.viam.feeder.data.api

import com.viam.feeder.data.models.KeyValue
import retrofit2.http.GET

interface EventService {

    @GET("settings/")
    suspend fun save(event: KeyValue)

}
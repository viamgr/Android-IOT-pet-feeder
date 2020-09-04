package com.viam.feeder.services

import com.viam.feeder.services.models.Status
import retrofit2.http.GET

interface GlobalConfigService {

    @GET("base/status")
    suspend fun getStatus(): Status
}
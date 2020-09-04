package com.viam.feeder.services

import com.viam.feeder.services.models.MotorStatusRequest
import com.viam.feeder.services.models.MotorStatusResponse
import com.viam.feeder.services.models.Status
import retrofit2.http.GET
import retrofit2.http.POST

interface GlobalConfigService {

    @GET("base/status")
    suspend fun getStatus(): Status

    @GET("motor/status")
    suspend fun getMotorStatus(): MotorStatusResponse

    @POST("motor/status")
    suspend fun setMotorStatus(status: MotorStatusRequest): MotorStatusResponse
}
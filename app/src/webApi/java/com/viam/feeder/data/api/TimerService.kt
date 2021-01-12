package com.viam.feeder.data.api

import com.viam.feeder.data.models.ClockTimer
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface TimerService {

    @GET("timer/list/")
    suspend fun getList(): List<ClockTimer>

    @POST("timer/")
    suspend fun add(clockTimer: ClockTimer): ClockTimer

    @DELETE("timer/{id}/")
    suspend fun delete(@Path("id") id: Int)
}
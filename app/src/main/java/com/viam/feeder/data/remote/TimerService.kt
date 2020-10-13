package com.viam.feeder.data.remote

import com.viam.feeder.data.models.ClockTimer
import retrofit2.http.GET

interface TimerService {

    @GET("timer/list/")
    suspend fun getTimes(): List<ClockTimer>
}
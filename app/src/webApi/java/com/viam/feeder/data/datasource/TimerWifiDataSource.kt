package com.viam.feeder.data.datasource

import com.viam.feeder.data.remote.TimerService
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TimerWifiDataSource @Inject constructor(private val timerService: TimerService) {
    suspend fun getTimes() = timerService.getTimes()
}
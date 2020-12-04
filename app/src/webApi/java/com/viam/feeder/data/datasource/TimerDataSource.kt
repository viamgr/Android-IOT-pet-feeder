package com.viam.feeder.data.datasource

import com.viam.feeder.data.api.TimerService
import com.viam.feeder.data.models.ClockTimer
import dagger.hilt.android.scopes.ActivityScoped
import javax.inject.Inject

@ActivityScoped
class TimerDataSource @Inject constructor(private val timerService: TimerService) {
    suspend fun getList() = timerService.getList()
    suspend fun add(clockTimer: ClockTimer) = timerService.add(clockTimer)
    suspend fun delete(clockTimer: ClockTimer) = timerService.delete(clockTimer.id)
}
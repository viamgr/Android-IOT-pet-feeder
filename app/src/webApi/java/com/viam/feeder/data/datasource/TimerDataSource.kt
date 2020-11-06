package com.viam.feeder.data.datasource

import com.viam.feeder.data.models.ClockTimer
import com.viam.feeder.data.remote.TimerService
import dagger.hilt.android.scopes.ActivityScoped
import javax.inject.Inject

@ActivityScoped
class TimerDataSource @Inject constructor(private val timerService: TimerService) {
    suspend fun getList() = timerService.getList()
    fun addTime(clockTimer: ClockTimer) = timerService.addTime(clockTimer)
    fun removeTime(clockTimer: ClockTimer) = timerService.removeTime(clockTimer.id)
}
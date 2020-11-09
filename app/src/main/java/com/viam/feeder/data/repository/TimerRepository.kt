package com.viam.feeder.data.repository

import com.viam.feeder.data.datasource.TimerDataSource
import com.viam.feeder.data.models.ClockTimer
import dagger.hilt.android.scopes.ActivityScoped
import javax.inject.Inject

@ActivityScoped
class TimerRepository @Inject constructor(private val timerDataSource: TimerDataSource) {
    suspend fun getList() = timerDataSource.getList()

    suspend fun add(clockTimer: ClockTimer) = timerDataSource.add(clockTimer)

    suspend fun delete(clockTimer: ClockTimer) = timerDataSource.delete(clockTimer)
}
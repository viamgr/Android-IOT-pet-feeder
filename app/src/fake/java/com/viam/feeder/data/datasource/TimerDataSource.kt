package com.viam.feeder.data.datasource

import com.viam.feeder.R
import com.viam.feeder.data.models.ClockTimer
import com.viam.feeder.data.utils.fakeRequest
import dagger.hilt.android.scopes.ActivityScoped
import javax.inject.Inject

@ActivityScoped
class TimerDataSource @Inject constructor() {
    private val list = mutableListOf(
        ClockTimer(1, 23, 10, R.string.am),
        ClockTimer(2, 5, 1, R.string.pm),
    )

    suspend fun getList(): List<ClockTimer> = fakeRequest {
        list
    }

    suspend fun add(clockTimer: ClockTimer): ClockTimer = fakeRequest {
        clockTimer.id = (list.size + 1).toLong()
        list.add(clockTimer)
        clockTimer
    }

    suspend fun delete(clockTimer: ClockTimer): Unit = fakeRequest {
        list.remove(clockTimer)
    }
}
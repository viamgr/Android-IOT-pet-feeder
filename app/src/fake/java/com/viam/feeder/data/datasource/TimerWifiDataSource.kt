package com.viam.feeder.data.datasource

import com.viam.feeder.R
import com.viam.feeder.data.models.ClockTimer
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TimerWifiDataSource @Inject constructor() {
    private val list = mutableListOf(
        ClockTimer(1, 23, 10, R.string.am),
        ClockTimer(2, 5, 1, R.string.pm),
    )

    fun getTimes(): List<ClockTimer> {
        return list
    }

    fun addTime(clockTimer: ClockTimer): ClockTimer {
        clockTimer.id = (list.size + 1).toLong()
        list.add(clockTimer)
        return clockTimer
    }

    fun removeTime(clockTimer: ClockTimer) {
        list.remove(clockTimer)
    }
}
package com.viam.feeder.data.repository

import com.viam.feeder.core.Resource
import com.viam.feeder.core.network.safeApiCall
import com.viam.feeder.data.datasource.TimerDataSource
import com.viam.feeder.data.models.ClockTimer
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TimerRepository @Inject constructor(private val timerWifiDataSource: TimerDataSource) {
    suspend fun getTimes(): Resource<List<ClockTimer>> {
        return safeApiCall {
            timerWifiDataSource.getTimes()
        }
    }

    suspend fun addTime(clockTimer: ClockTimer): Resource<ClockTimer> {
        return safeApiCall {
            timerWifiDataSource.addTime(clockTimer)
        }
    }

    suspend fun removeTime(clockTimer: ClockTimer): Resource<Unit> {
        return safeApiCall {
            timerWifiDataSource.removeTime(clockTimer)
        }
    }
}
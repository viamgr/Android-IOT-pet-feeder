package com.viam.feeder.data.repository

import com.viam.feeder.core.Resource
import com.viam.feeder.core.network.safeApiCall
import com.viam.feeder.data.datasource.TimerDataSource
import com.viam.feeder.data.models.ClockTimer
import dagger.hilt.android.scopes.ActivityScoped
import javax.inject.Inject

@ActivityScoped
class TimerRepository @Inject constructor(private val timerDataSource: TimerDataSource) {
    suspend fun getTimes(): Resource<List<ClockTimer>> {
        return safeApiCall {
            timerDataSource.getTimes()
        }
    }

    suspend fun addTime(clockTimer: ClockTimer): Resource<ClockTimer> {
        return safeApiCall {
            timerDataSource.addTime(clockTimer)
        }
    }

    suspend fun removeTime(clockTimer: ClockTimer): Resource<Unit> {
        return safeApiCall {
            timerDataSource.removeTime(clockTimer)
        }
    }
}
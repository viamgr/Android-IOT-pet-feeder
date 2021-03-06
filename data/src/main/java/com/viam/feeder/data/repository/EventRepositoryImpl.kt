package com.viam.feeder.data.repository

import com.viam.feeder.data.datasource.EventDataSource
import dagger.hilt.android.scopes.ActivityScoped
import javax.inject.Inject

@ActivityScoped
class EventRepositoryImpl @Inject constructor(private val eventDataSource: EventDataSource) {
    suspend fun sendEvent(event: String) = eventDataSource.sendEvent(event)
    suspend fun setStatus(key: String, value: String) = eventDataSource.setState(key, value)
    suspend fun getStatus(key: String) = eventDataSource.getState(key)
}
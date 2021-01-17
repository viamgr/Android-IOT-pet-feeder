package com.viam.feeder.data.repository

import com.viam.feeder.data.datasource.EventDataSource
import dagger.hilt.android.scopes.ActivityScoped
import javax.inject.Inject

@ActivityScoped
class EventRepository @Inject constructor(private val eventDataSource: EventDataSource) {
    suspend fun sendEvent(event: String) = eventDataSource.sendEvent(event)
}
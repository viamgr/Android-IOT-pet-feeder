package com.viam.feeder.data.datasource

import com.viam.feeder.data.api.EventService
import dagger.hilt.android.scopes.ActivityScoped
import javax.inject.Inject

@ActivityScoped
class EventDataSource @Inject constructor(private val eventService: EventService) {
    suspend fun sendEvent(event: String) = eventService.send(event)
}
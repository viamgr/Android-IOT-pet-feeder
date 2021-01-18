package com.viam.feeder.data.datasource

import com.viam.feeder.data.api.EventService
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class EventDataSourceImpl @Inject constructor(private val eventService: EventService) :
    EventDataSource {
    override suspend fun sendEvent(event: String) = eventService.send(event)
}
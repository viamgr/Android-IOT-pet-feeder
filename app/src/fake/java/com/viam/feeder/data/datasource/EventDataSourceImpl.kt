package com.viam.feeder.data.datasource

import com.viam.feeder.data.utils.fakeRequest
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class EventDataSourceImpl @Inject constructor() : EventDataSource {
    override suspend fun sendEvent(event: String) = fakeRequest {

    }
}
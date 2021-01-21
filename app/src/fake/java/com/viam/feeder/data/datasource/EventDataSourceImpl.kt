package com.viam.feeder.data.datasource

import com.viam.feeder.data.utils.fakeRequest
import com.viam.feeder.ui.wifi.NetworkStatusObserver
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class EventDataSourceImpl @Inject constructor(private val networkStatusObserver: NetworkStatusObserver) :
    EventDataSource {
    override suspend fun sendEvent(event: String) = fakeRequest(networkStatusObserver) {

    }
}
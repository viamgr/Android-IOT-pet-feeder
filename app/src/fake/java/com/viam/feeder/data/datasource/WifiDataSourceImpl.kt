package com.viam.feeder.data.datasource

import com.viam.feeder.data.models.WifiDevice
import com.viam.feeder.data.utils.fakeRequest
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WifiDataSourceImpl @Inject constructor() : WifiDataSource {
    private val list = mutableListOf(
        WifiDevice("Wifi Name", "Wifi Name", 8),
        WifiDevice("New Wifi", "New Wifi", 7)
    )

    override suspend fun getList() = fakeRequest {
        list
    }

    override suspend fun connect(ssid: String, password: String) = fakeRequest {

    }
}
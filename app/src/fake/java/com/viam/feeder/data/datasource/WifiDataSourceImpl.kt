package com.viam.feeder.data.datasource

import com.viam.feeder.data.models.WifiDevice
import com.viam.feeder.data.utils.fakeRequest
import com.viam.feeder.ui.wifi.NetworkStatusObserver
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WifiDataSourceImpl @Inject constructor(private val networkStatusObserver: NetworkStatusObserver) :
    WifiDataSource {
    private val list = mutableListOf(
        WifiDevice("Wifi Name", "Wifi Name", 8),
        WifiDevice("New Wifi", "New Wifi", 7)
    )

    override suspend fun getList() = fakeRequest(networkStatusObserver) {
        list
    }

    override suspend fun connect(ssid: String, password: String) =
        fakeRequest(networkStatusObserver) {

        }
}
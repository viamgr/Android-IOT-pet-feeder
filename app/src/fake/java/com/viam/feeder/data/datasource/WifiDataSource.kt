package com.viam.feeder.data.datasource

import com.viam.feeder.data.models.WifiDevice
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WifiDataSource @Inject constructor() {
    private val list = mutableListOf(
        WifiDevice("V. M", "V. M", true),
        WifiDevice("New Wifi", "New Wifi", false)
    )

    fun getList() = list

    fun connect(ssid: String, password: String) {

    }
}
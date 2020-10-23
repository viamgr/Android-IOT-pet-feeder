package com.viam.feeder.data.datasource

import com.viam.feeder.data.models.WifiDevice
import dagger.hilt.android.scopes.ActivityScoped
import javax.inject.Inject

@ActivityScoped
class WifiDataSource @Inject constructor() {
    private val list = mutableListOf(
        WifiDevice("V. M", "V. M", true),
        WifiDevice("New Wifi", "New Wifi", false)
    )

    fun getList() = list

    fun connect(ssid: String, password: String) {

    }
}
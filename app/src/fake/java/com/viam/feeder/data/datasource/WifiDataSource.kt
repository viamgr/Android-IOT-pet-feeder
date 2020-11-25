package com.viam.feeder.data.datasource

import com.viam.feeder.data.models.WifiDevice
import com.viam.feeder.data.utils.randomException
import dagger.hilt.android.scopes.ActivityScoped
import javax.inject.Inject

@ActivityScoped
class WifiDataSource @Inject constructor() {
    private val list = mutableListOf(
        WifiDevice("V. M", "V. M", true),
        WifiDevice("New Wifi", "New Wifi", false)
    )

    suspend fun getList() = randomException {
        list
    }

    suspend fun connect(ssid: String, password: String) = randomException {

    }
}
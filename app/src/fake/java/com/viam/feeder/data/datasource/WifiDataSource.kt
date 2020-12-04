package com.viam.feeder.data.datasource

import com.viam.feeder.data.models.WifiDevice
import com.viam.feeder.data.utils.fakeRequest
import dagger.hilt.android.scopes.ActivityScoped
import javax.inject.Inject

@ActivityScoped
class WifiDataSource @Inject constructor() {
    private val list = mutableListOf(
        WifiDevice("Wifi Name", "Wifi Name", true),
        WifiDevice("New Wifi", "New Wifi", false)
    )

    suspend fun getList() = fakeRequest {
        list
    }

    suspend fun connect(ssid: String, password: String) = fakeRequest {

    }
}
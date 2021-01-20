package com.viam.feeder.data.datasource

import android.content.Context
import com.viam.feeder.data.models.WifiDevice
import com.viam.feeder.data.utils.fakeRequest
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WifiDataSourceImpl @Inject constructor(@ApplicationContext private val context: Context) :
    WifiDataSource {
    private val list = mutableListOf(
        WifiDevice("Wifi Name", "Wifi Name", 8),
        WifiDevice("New Wifi", "New Wifi", 7)
    )

    override suspend fun getList() = fakeRequest(context) {
        list
    }

    override suspend fun connect(ssid: String, password: String) = fakeRequest(context) {

    }
}
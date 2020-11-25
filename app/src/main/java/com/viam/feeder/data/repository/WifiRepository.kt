package com.viam.feeder.data.repository

import com.viam.feeder.data.datasource.WifiDataSource
import dagger.hilt.android.scopes.ActivityScoped
import javax.inject.Inject

@ActivityScoped
class WifiRepository @Inject constructor(private val wifiDataSource: WifiDataSource) {
    suspend fun getList() = wifiDataSource.getList()

    suspend fun connect(ssid: String, password: String) = wifiDataSource.connect(ssid, password)
}
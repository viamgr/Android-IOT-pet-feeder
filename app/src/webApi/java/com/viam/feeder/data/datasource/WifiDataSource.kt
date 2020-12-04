package com.viam.feeder.data.datasource

import com.viam.feeder.data.api.WifiService
import dagger.hilt.android.scopes.ActivityScoped
import javax.inject.Inject

@ActivityScoped
class WifiDataSource @Inject constructor(private val wifiService: WifiService) {
    suspend fun getList() = wifiService.list()
    suspend fun connect(ssid: String, password: String) = wifiService.connect(ssid, password)
}
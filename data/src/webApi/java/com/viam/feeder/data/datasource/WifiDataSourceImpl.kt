package com.viam.feeder.data.datasource

import com.viam.feeder.data.api.WifiService
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WifiDataSourceImpl @Inject constructor(private val wifiService: WifiService) :
    WifiDataSource {
    override suspend fun getList() = wifiService.list()
    override suspend fun connect(ssid: String, password: String) =
        wifiService.connect(ssid, password)
}
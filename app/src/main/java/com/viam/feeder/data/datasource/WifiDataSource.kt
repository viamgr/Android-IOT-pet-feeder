package com.viam.feeder.data.datasource

import com.viam.feeder.data.models.WifiDevice

interface WifiDataSource {
    suspend fun getList(): List<WifiDevice>
    suspend fun connect(ssid: String, password: String)
}
package com.viam.feeder.data.datasource

interface WifiDataSource {
    suspend fun getList(): List<com.viam.feeder.model.WifiDevice>
    suspend fun connect(ssid: String, password: String)
}
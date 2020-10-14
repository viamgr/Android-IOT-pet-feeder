package com.viam.feeder.data.repository

import com.viam.feeder.core.Resource
import com.viam.feeder.core.network.safeApiCall
import com.viam.feeder.data.datasource.WifiDataSource
import com.viam.feeder.data.models.WifiDevice
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WifiRepository @Inject constructor(private val wifiDataSource: WifiDataSource) {
    suspend fun getList(): Resource<List<WifiDevice>> {
        return safeApiCall {
            wifiDataSource.getList()
        }
    }

    suspend fun connect(ssid: String, password: String): Resource<Unit> {
        return safeApiCall {
            wifiDataSource.connect(ssid, password)
        }
    }
}
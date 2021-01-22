package com.viam.feeder.data.datasource

interface EventDataSource {
    suspend fun sendEvent(event: String)
    suspend fun setState(key: String, value: Any)
    suspend fun getState(key: String): String
}
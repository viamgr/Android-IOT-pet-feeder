package com.viam.feeder.data.datasource

interface EventDataSource {
    suspend fun sendEvent(event: String)
}
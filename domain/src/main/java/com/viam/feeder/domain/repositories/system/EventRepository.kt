package com.viam.feeder.domain.repositories.system

interface EventRepository {
    suspend fun sendEvent(event: String)
    suspend fun setStatus(key: String, value: String)
    suspend fun getStatus(key: String): String
}
package com.viam.feeder.data.repository

import com.viam.feeder.data.constants.EVENT_WIFI_CONNECT
import com.viam.feeder.data.datasource.WifiDataSource
import com.viam.feeder.domain.repositories.system.EventRepository
import dagger.hilt.android.scopes.ActivityScoped
import javax.inject.Inject

@ActivityScoped
class WifiRepository @Inject constructor(
    private val wifiDataSource: WifiDataSource,
    private val eventRepository: EventRepository,

    ) {
    suspend fun getList() = wifiDataSource.getList()

    suspend fun connect() = eventRepository.sendEvent(EVENT_WIFI_CONNECT)
}
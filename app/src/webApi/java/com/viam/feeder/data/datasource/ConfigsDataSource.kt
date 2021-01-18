package com.viam.feeder.data.datasource

import com.viam.feeder.data.api.ConfigsService
import com.viam.feeder.data.utils.bodyOrThrow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ConfigsDataSource @Inject constructor(private val downloadResourceService: ConfigsService) {
    suspend fun downloadConfigs() =
        downloadResourceService.downloadConfigs().bodyOrThrow().byteStream()
}
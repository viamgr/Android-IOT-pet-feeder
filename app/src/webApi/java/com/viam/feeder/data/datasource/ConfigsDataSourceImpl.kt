package com.viam.feeder.data.datasource

import com.viam.feeder.data.api.ConfigsService
import com.viam.feeder.data.utils.bodyOrThrow
import javax.inject.Inject
import javax.inject.Singleton
@Singleton
class ConfigsDataSourceImpl @Inject constructor(private val downloadResourceService: ConfigsService) :
    ConfigsDataSource {
    override suspend fun downloadConfigs() =
        downloadResourceService.downloadConfigs().bodyOrThrow().byteStream()
}
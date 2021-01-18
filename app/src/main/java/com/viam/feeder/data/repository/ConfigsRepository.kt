package com.viam.feeder.data.repository

import com.viam.feeder.data.datasource.ConfigsDataSource
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ConfigsRepository @Inject constructor(private val configsDataSource: ConfigsDataSource) {
    suspend fun downloadConfigs() = configsDataSource.downloadConfigs()
}
package com.viam.feeder.data.repository

import com.viam.feeder.data.datasource.ConfigsDataSource
import dagger.hilt.android.scopes.ActivityScoped
import javax.inject.Inject

@ActivityScoped
class ConfigsRepository @Inject constructor(private val configsDataSource: ConfigsDataSource) {
    suspend fun downloadConfigs() = configsDataSource.downloadConfigs()
}
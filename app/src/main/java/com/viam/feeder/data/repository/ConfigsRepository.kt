package com.viam.feeder.data.repository

import com.viam.feeder.data.datasource.ConfigsDataSource
import dagger.hilt.android.scopes.ActivityScoped
import okhttp3.MultipartBody
import javax.inject.Inject

@ActivityScoped
class ConfigsRepository @Inject constructor(private val configsDataSource: ConfigsDataSource) {
    suspend fun downloadConfigs() = configsDataSource.downloadConfigs()
    suspend fun uploadConfigs(configs: MultipartBody.Part) =
        configsDataSource.uploadConfigs(configs)
}
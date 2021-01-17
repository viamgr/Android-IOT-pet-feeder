package com.viam.feeder.data.datasource

import com.viam.feeder.data.api.ConfigsService
import com.viam.feeder.data.utils.bodyOrThrow
import dagger.hilt.android.scopes.ActivityScoped
import javax.inject.Inject

@ActivityScoped
class ConfigsDataSource @Inject constructor(private val downloadResourceService: ConfigsService) {

    @Synchronized
    suspend fun downloadConfigs() =
        downloadResourceService.downloadConfigs().bodyOrThrow().byteStream()
}
package com.viam.feeder.data.repository

import com.viam.feeder.data.datasource.SettingDataSource
import com.viam.feeder.data.models.KeyValue
import dagger.hilt.android.scopes.ActivityScoped
import javax.inject.Inject

@ActivityScoped
class SettingRepository @Inject constructor(private val settingDataSource: SettingDataSource) {
    suspend fun list() = settingDataSource.list()
    suspend fun update(setting: KeyValue) = settingDataSource.update(setting)
}
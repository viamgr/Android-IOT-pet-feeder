package com.viam.feeder.di

import com.viam.feeder.data.datasource.*
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ApplicationComponent


@Module
@InstallIn(ApplicationComponent::class)
abstract class DataSourceModule {
    @Binds
    abstract fun bindWifiDataSourceImpl(dataSource: WifiDataSourceImpl): WifiDataSource

    @Binds
    abstract fun bindUploadDataSourceImpl(dataSource: UploadDataSourceImpl): UploadDataSource

    @Binds
    abstract fun bindEventDataSourceImpl(dataSource: EventDataSourceImpl): EventDataSource

    @Binds
    abstract fun bindConfigsDataSourceImpl(dataSource: ConfigsDataSourceImpl): ConfigsDataSource
}
package com.viam.feeder.data.di

import com.viam.feeder.data.database.datasources.DeviceLocalDataSource
import com.viam.feeder.data.database.datasources.DeviceLocalDataSourceImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class LocalDataSourceModule {

    @Binds
    abstract fun bindDeviceLocalDataSource(impl: DeviceLocalDataSourceImpl): DeviceLocalDataSource
}
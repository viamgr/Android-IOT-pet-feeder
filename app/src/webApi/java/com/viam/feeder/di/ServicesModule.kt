package com.viam.feeder.di

import com.viam.feeder.data.api.ConfigsService
import com.viam.feeder.data.api.EventService
import com.viam.feeder.data.api.UploadService
import com.viam.feeder.data.api.WifiService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import javax.inject.Singleton


@Module
@InstallIn(SingletonComponent::class)
class ServicesModule {

    @Singleton
    @Provides
    fun provideUploadService(retroFit: Retrofit): UploadService {
        return retroFit.create(UploadService::class.java)
    }

    @Singleton
    @Provides
    fun provideEventService(retroFit: Retrofit): EventService {
        return retroFit.create(EventService::class.java)
    }

    @Singleton
    @Provides
    fun provideConfigsService(retroFit: Retrofit): ConfigsService {
        return retroFit.create(ConfigsService::class.java)
    }

    @Singleton
    @Provides
    fun provideWifiService(retroFit: Retrofit): WifiService {
        return retroFit.create(WifiService::class.java)
    }

}
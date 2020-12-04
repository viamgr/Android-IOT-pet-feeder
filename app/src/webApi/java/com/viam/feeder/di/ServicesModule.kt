package com.viam.feeder.di

import com.viam.feeder.data.api.EventService
import com.viam.feeder.data.api.TimerService
import com.viam.feeder.data.api.UploadService
import com.viam.feeder.data.api.WifiService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityComponent
import dagger.hilt.android.scopes.ActivityScoped
import retrofit2.Retrofit


@Module
@InstallIn(ActivityComponent::class)
class ServicesModule {

    @ActivityScoped
    @Provides
    fun provideTimerService(retroFit: Retrofit): TimerService =
        retroFit.create(TimerService::class.java)

    @ActivityScoped
    @Provides
    fun provideUploadService(retroFit: Retrofit): UploadService {
        return retroFit.create(UploadService::class.java)
    }

    @ActivityScoped
    @Provides
    fun provideEventService(retroFit: Retrofit): EventService {
        return retroFit.create(EventService::class.java)
    }

    @ActivityScoped
    @Provides
    fun provideWifiService(retroFit: Retrofit): WifiService {
        return retroFit.create(WifiService::class.java)
    }

}
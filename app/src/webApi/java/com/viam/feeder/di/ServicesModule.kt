package com.viam.feeder.di

import com.viam.feeder.data.api.GlobalConfigService
import com.viam.feeder.data.api.TimerService
import com.viam.feeder.data.api.UploadService
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
    fun provideGlobalConfigService(retroFit: Retrofit): GlobalConfigService =
        retroFit.create(GlobalConfigService::class.java)

    @ActivityScoped
    @Provides
    fun provideTimerService(retroFit: Retrofit): TimerService =
        retroFit.create(TimerService::class.java)

    @ActivityScoped
    @Provides
    fun provideUploadService(retroFit: Retrofit): UploadService {
        return retroFit.create(UploadService::class.java)
    }

}
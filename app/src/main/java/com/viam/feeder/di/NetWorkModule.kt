package com.viam.feeder.di

import com.viam.feeder.services.GlobalConfigService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityComponent
import dagger.hilt.android.scopes.ActivityScoped
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory


@Module
@InstallIn(ActivityComponent::class)
class NetWorkModule {
    companion object {
        const val API_IP = "192.168.4.1"
        const val API_PORT = 80
    }

    @ActivityScoped
    @Provides
    fun bindGlobalConfigService(retroFit: Retrofit): GlobalConfigService {
        return retroFit.create(GlobalConfigService::class.java)
    }

    @ActivityScoped
    @Provides
    fun getRetrofit(okHttpClient: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl("http://$API_IP/")
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create())
            .build()
    }

    @Provides
    @ActivityScoped
    fun getHttpClient(): OkHttpClient {
        val okHttpBuilder = OkHttpClient.Builder()
        return okHttpBuilder.build()
    }

}
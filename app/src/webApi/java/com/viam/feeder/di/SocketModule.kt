package com.viam.feeder.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ApplicationComponent
import okhttp3.OkHttpClient
import okhttp3.Request
import java.util.concurrent.TimeUnit
import javax.inject.Named
import javax.inject.Singleton


@Module
@InstallIn(ApplicationComponent::class)
class SocketModule {

    @Provides
    @Singleton
    @Named("socket")
    fun getHttpClient(
    ): OkHttpClient {
        return OkHttpClient.Builder()
            .retryOnConnectionFailure(true)
            .readTimeout(2, TimeUnit.SECONDS)
            .connectTimeout(2, TimeUnit.SECONDS)
            .build()
    }

    @Provides
    @Singleton
    fun getSocketRequest(
    ): Request {
        return Request.Builder().url("ws://$SOCKET_URL:$SOCKET_PORT").build()
    }

    companion object {
        const val SOCKET_URL = "192.168.4.1"
        const val SOCKET_PORT = 4200
    }
}
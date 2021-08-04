package com.viam.feeder.di

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.squareup.moshi.Moshi
import com.viam.websocket.WebSocketApi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.Request
import java.util.concurrent.TimeUnit
import javax.inject.Named
import javax.inject.Singleton


@Module
@InstallIn(SingletonComponent::class)
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

    @Provides
    @Singleton
    fun provideWebSocketApi(
        @Named("socket") okHttpClient: OkHttpClient,
        moshi: Moshi,
        request: Request
    ): WebSocketApi {
        return WebSocketApi(okHttpClient, moshi, request)
    }

    @Provides
    @Singleton
    fun provideLiveData(
        webSocketApi: WebSocketApi
    ): LiveData<Boolean> {
        val a = MutableLiveData<Boolean>(false)
        webSocketApi.setOnConnectionChangedListener {
            a.value = it
        }
        return a
    }

    companion object {
        const val SOCKET_URL = "192.168.4.1"
        const val SOCKET_PORT = 4200
    }
}
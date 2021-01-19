package com.viam.feeder.di

import com.facebook.flipper.plugins.network.FlipperOkhttpInterceptor
import com.facebook.flipper.plugins.network.NetworkFlipperPlugin
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ApplicationComponent
import okhttp3.OkHttpClient
import okhttp3.Protocol
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton


@Module
@InstallIn(ApplicationComponent::class)
class NetWorkModule {

    @Provides
    @Singleton
    fun getHttpClient(
        networkFlipperPlugin: NetworkFlipperPlugin
    ): OkHttpClient {
        val okHttpBuilder = OkHttpClient.Builder()

        val interceptor = HttpLoggingInterceptor()
        interceptor.apply { interceptor.level = HttpLoggingInterceptor.Level.HEADERS }

        return okHttpBuilder
            .retryOnConnectionFailure(true)
            .addInterceptor(interceptor)
            .callTimeout(100, TimeUnit.SECONDS)
            .protocols(listOf(Protocol.HTTP_1_1))

            .connectTimeout(100, TimeUnit.SECONDS)
            .readTimeout(100, TimeUnit.SECONDS)
            .writeTimeout(100, TimeUnit.SECONDS)
            .addNetworkInterceptor(FlipperOkhttpInterceptor(networkFlipperPlugin))
            .build()
    }

    @Singleton
    @Provides
    fun getRetrofit(okHttpClient: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create())
            .build()
    }

    @Provides
    @Singleton
    fun provideNetworkFlipperPlugin(): NetworkFlipperPlugin = NetworkFlipperPlugin()

    companion object {
        const val BASE_URL = "http://192.168.4.1/"
        const val API_PORT = 80
    }

}
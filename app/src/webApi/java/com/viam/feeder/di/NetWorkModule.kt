package com.viam.feeder.di

import com.facebook.flipper.plugins.network.FlipperOkhttpInterceptor
import com.facebook.flipper.plugins.network.NetworkFlipperPlugin
import com.squareup.moshi.Moshi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ApplicationComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton


@Module
@InstallIn(ApplicationComponent::class)
class NetWorkModule {
    companion object {
        const val API_IP = "192.168.4.1"
        const val API_PORT = 80
    }

    @Singleton
    @Provides
    fun getRetrofit(okHttpClient: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl("http://$API_IP/")
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create())
            .build()
    }

    @Provides
    @Singleton
    fun provideNetworkFlipperPlugin(): NetworkFlipperPlugin = NetworkFlipperPlugin()

    @Provides
    @Singleton
    fun getHttpClient(
        networkFlipperPlugin: NetworkFlipperPlugin
    ): OkHttpClient {
        val okHttpBuilder = OkHttpClient.Builder()

        val interceptor = HttpLoggingInterceptor()
        interceptor.apply { interceptor.level = HttpLoggingInterceptor.Level.HEADERS }

        return okHttpBuilder
            .retryOnConnectionFailure(false)
//            .addInterceptor(interceptor)
            .callTimeout(10, TimeUnit.SECONDS)
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(10, TimeUnit.SECONDS)
            .writeTimeout(10, TimeUnit.SECONDS)
            .addNetworkInterceptor(FlipperOkhttpInterceptor(networkFlipperPlugin))
            .build()
    }

}
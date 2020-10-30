package com.viam.feeder.di

import com.viam.feeder.data.remote.GlobalConfigService
import com.viam.feeder.data.remote.TimerService
import com.viam.feeder.data.remote.UploadService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityComponent
import dagger.hilt.android.scopes.ActivityScoped
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.concurrent.TimeUnit


@Module
@InstallIn(ActivityComponent::class)
class NetWorkModule {
    companion object {
        const val API_IP = "192.168.4.1"
        const val API_PORT = 80
    }

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

        val interceptor = HttpLoggingInterceptor()
        interceptor.apply { interceptor.level = HttpLoggingInterceptor.Level.HEADERS }

        return okHttpBuilder
            .retryOnConnectionFailure(false)
//            .addInterceptor(interceptor)
            .callTimeout(10, TimeUnit.SECONDS)
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(10, TimeUnit.SECONDS)
            .writeTimeout(10, TimeUnit.SECONDS)
            .build()
    }

}
package com.viam.feeder.di

import com.viam.feeder.data.datasource.RemoteConnectionConfig
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DynamicUrlInterceptor @Inject constructor(val remoteConnectionConfig: RemoteConnectionConfig) :
    Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {

        var request = chain.request()

        val host: String = remoteConnectionConfig.url

        val newUrl = request.url.newBuilder()
            .host(host)
            .build()

        request = request.newBuilder()
            .url(newUrl)
            .build()

        return chain.proceed(request)
    }
}
package com.viam.feeder.di

import android.content.Context
import com.part.livetaskcore.LiveTaskManager
import com.part.livetaskcore.MultipleNoConnectionInformer
import com.part.livetaskcore.WebConnectionChecker
import com.part.livetaskcore.connection.ConnectionManager
import com.squareup.moshi.Moshi
import com.viam.feeder.SocketConnectionChecker
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ApplicationComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import javax.inject.Named
import javax.inject.Singleton


@Module
@InstallIn(ApplicationComponent::class)
class AppModule {

    @Provides
    @Singleton
    fun provideMoshi() = Moshi.Builder().build()

    @Provides
    @Singleton
    fun provideLiveTaskManager() = LiveTaskManager()

    @Provides
    @Singleton
    fun provideConnectionManager(@ApplicationContext context: Context) = ConnectionManager(context)

    @Provides
    @Singleton
    fun provideWebConnectionChecker(connectionManager: ConnectionManager) =
        WebConnectionChecker(connectionManager)

    @Provides
    @Singleton
    fun provideMultipleNoConnectionInformer(
        socketConnectionChecker: SocketConnectionChecker,
        webConnectionChecker: WebConnectionChecker
    ) = MultipleNoConnectionInformer(
        socketConnectionChecker,
        webConnectionChecker
    )

    @Provides
    @Singleton
    @Named("configFile")
    fun provideConfigFile(@ApplicationContext context: Context) =
        File(context.cacheDir, configFilePath)

    companion object {
        const val configFilePath = "config.json"
    }
}
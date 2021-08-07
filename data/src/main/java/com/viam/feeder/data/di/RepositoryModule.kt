package com.viam.feeder.data.di

import com.viam.feeder.data.repository.ConfigFieldsImpl
import com.viam.feeder.data.repository.FfmpegRepositoryImpl
import com.viam.feeder.data.repository.JsonPreferencesImpl
import com.viam.feeder.data.repository.WebSocketRepositoryImpl
import com.viam.feeder.domain.repositories.socket.FfmpegRepository
import com.viam.feeder.domain.repositories.socket.WebSocketRepository
import com.viam.feeder.domain.repositories.system.ConfigFields
import com.viam.feeder.domain.repositories.system.JsonPreferences
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent


@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    abstract fun bindConfigFields(repository: ConfigFieldsImpl): ConfigFields

    @Binds
    abstract fun bindJsonPreferences(repository: JsonPreferencesImpl): JsonPreferences

    @Binds
    abstract fun bindFfmpegRepositoryImpl(repository: FfmpegRepositoryImpl): FfmpegRepository

    @Binds
    abstract fun bindWebSocketRepositoryImpl(repository: WebSocketRepositoryImpl): WebSocketRepository

}
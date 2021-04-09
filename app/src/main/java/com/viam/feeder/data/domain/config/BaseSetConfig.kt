package com.viam.feeder.data.domain.config

import com.viam.feeder.core.domain.FlowUseCase
import com.viam.feeder.core.network.CoroutinesDispatcherProvider
import com.viam.feeder.core.utility.toResource
import com.viam.feeder.data.storage.JsonPreferences
import com.viam.feeder.di.AppModule.Companion.configFilePath
import com.viam.resource.Resource
import com.viam.websocket.WebSocketApi
import com.viam.websocket.model.SocketTransfer
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

abstract class BaseSetConfig<T>(
    private val coroutinesDispatcherProvider: CoroutinesDispatcherProvider,
    private val webSocketApi: WebSocketApi,
    private val jsonPreferences: JsonPreferences,
) : FlowUseCase<T, SocketTransfer>(coroutinesDispatcherProvider.io) {
    override fun execute(parameter: T): Flow<Resource<SocketTransfer>> {
        return flow {
            setConfigField(parameter)
            val inputStream = jsonPreferences.json.toString().byteInputStream()
            webSocketApi
                .sendBinary(configFilePath, inputStream)
                .map {
                    it.also {
                        if (it is SocketTransfer.Success) {
                            withContext(coroutinesDispatcherProvider.main) {
                                jsonPreferences.resetFromTemp()
                            }
                        }
                    }
                }.collect {
                    emit(it.toResource())
                }
        }
    }

    @Throws(RuntimeException::class)
    protected abstract suspend fun setConfigField(value: T)

}
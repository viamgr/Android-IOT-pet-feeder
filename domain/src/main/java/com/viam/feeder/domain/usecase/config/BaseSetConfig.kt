package com.viam.feeder.domain.usecase.config

import com.viam.feeder.domain.base.CoroutinesDispatcherProvider
import com.viam.feeder.domain.base.FlowUseCase
import com.viam.feeder.domain.base.toResource
import com.viam.feeder.domain.repositories.socket.WebSocketRepository
import com.viam.feeder.domain.repositories.system.JsonPreferences
import com.viam.feeder.shared.FeederConstants.Companion.CONFIG_FILE_PATH
import com.viam.resource.Resource
import com.viam.websocket.model.SocketTransfer
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

abstract class BaseSetConfig<T>(
    private val coroutinesDispatcherProvider: CoroutinesDispatcherProvider,
    private val webSocketRepository: WebSocketRepository,
    private val jsonPreferences: JsonPreferences,
) : FlowUseCase<T, SocketTransfer>(coroutinesDispatcherProvider.io) {
    override fun execute(parameter: T): Flow<Resource<SocketTransfer>> {
        return flow {
            setConfigField(parameter)
            val inputStream = jsonPreferences.getByteStream()
            webSocketRepository
                .sendBinary(CONFIG_FILE_PATH, inputStream)
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
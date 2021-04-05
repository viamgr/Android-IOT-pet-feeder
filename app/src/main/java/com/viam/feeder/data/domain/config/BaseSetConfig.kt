package com.viam.feeder.data.domain.config

import com.viam.feeder.data.storage.JsonPreferences
import com.viam.feeder.di.AppModule.Companion.configFilePath
import com.viam.feeder.socket.WebSocketApi
import com.viam.feeder.socket.model.SocketTransfer
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.*

abstract class BaseSetConfig<T>(
    private val coroutineDispatcher: CoroutineDispatcher,
    private val webSocketApi: WebSocketApi,
    private val jsonPreferences: JsonPreferences,
) {
    suspend operator fun invoke(parameters: T): Flow<SocketTransfer> {
        setConfigField(parameters)
        val inputStream = jsonPreferences.json.toString().byteInputStream()
        return webSocketApi.sendBinary(configFilePath, inputStream)
            .flowOn(coroutineDispatcher)
            .map {
                it.also {
                    if (it is SocketTransfer.Success) {
                        jsonPreferences.resetFromTemp()
                    }
                }
            }.catch { e ->
                emit(SocketTransfer.Error(e))
            }.also { flow ->
                flow.collect()
            }
    }

    @Throws(RuntimeException::class)
    protected abstract suspend fun setConfigField(value: T)

}
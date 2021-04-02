package com.viam.feeder.data.domain.config

import com.viam.feeder.socket.WebSocketApi
import com.viam.feeder.socket.model.SocketTransfer
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flowOn
import java.io.OutputStream

open class DownloadBinary(
    private val coroutineDispatcher: CoroutineDispatcher,
    private val webSocketApi: WebSocketApi,
) {
    suspend operator fun invoke(parameters: ConfigParams): Flow<SocketTransfer> {
        return webSocketApi.receiveBinary(parameters.remotePath, parameters.outputStream)
            .flowOn(coroutineDispatcher)
            .catch { e ->
                emit(SocketTransfer.Error(e))
            }
    }

    data class ConfigParams(val remotePath: String, val outputStream: OutputStream)
}

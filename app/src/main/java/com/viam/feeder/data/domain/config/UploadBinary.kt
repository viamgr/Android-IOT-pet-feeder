package com.viam.feeder.data.domain.config

import com.viam.feeder.core.network.CoroutinesDispatcherProvider
import com.viam.feeder.socket.WebSocketApi
import com.viam.feeder.socket.model.SocketTransfer
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flowOn
import java.io.InputStream
import javax.inject.Inject

class UploadBinary @Inject constructor(
    private val coroutinesDispatcherProvider: CoroutinesDispatcherProvider,
    private val webSocketApi: WebSocketApi
) {
    suspend operator fun invoke(parameters: UploadBinaryParams): Flow<SocketTransfer> {
        return webSocketApi.sendBinary(parameters.remotePath, parameters.inputStream)
            .flowOn(coroutinesDispatcherProvider.io)
            .catch { e ->
                emit(SocketTransfer.Error(e))
            }
    }

    data class UploadBinaryParams(val remotePath: String, val inputStream: InputStream)
}

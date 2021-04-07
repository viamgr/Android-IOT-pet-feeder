package com.viam.feeder.data.domain.config

import com.part.livetaskcore.usecases.FlowUseCase
import com.viam.feeder.core.network.CoroutinesDispatcherProvider
import com.viam.feeder.core.utility.toResource
import com.viam.resource.Resource
import com.viam.websocket.WebSocketApi
import com.viam.websocket.model.SocketTransfer
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.io.OutputStream

open class DownloadBinary(
    coroutinesDispatcherProvider: CoroutinesDispatcherProvider,
    private val webSocketApi: WebSocketApi,
) : FlowUseCase<DownloadBinary.DownloadBinaryParams, SocketTransfer>(coroutinesDispatcherProvider.io) {
    override suspend fun execute(params: DownloadBinaryParams): Flow<Resource<SocketTransfer>> {
        return webSocketApi.receiveBinary(params.remotePath, params.outputStream).map {
            it.toResource()
        }
    }

    data class DownloadBinaryParams(val remotePath: String, val outputStream: OutputStream)
}

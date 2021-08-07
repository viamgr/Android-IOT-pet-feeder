package com.viam.feeder.domain.usecase.config

import com.viam.feeder.domain.base.CoroutinesDispatcherProvider
import com.viam.feeder.domain.base.FlowUseCase
import com.viam.feeder.domain.base.toResource
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
    override fun execute(parameter: DownloadBinaryParams): Flow<Resource<SocketTransfer>> {
        return webSocketApi.receiveBinary(parameter.remotePath, parameter.outputStream).map {
            it.toResource()
        }
    }

    data class DownloadBinaryParams(val remotePath: String, val outputStream: OutputStream)
}

package com.viam.feeder.domain.usecase.config

import com.viam.feeder.domain.base.CoroutinesDispatcherProvider
import com.viam.feeder.domain.base.FlowUseCase
import com.viam.feeder.domain.base.toResource
import com.viam.feeder.domain.repositories.socket.WebSocketRepository
import com.viam.resource.Resource
import com.viam.websocket.model.SocketTransfer
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.io.File

open class DownloadBinary(
    coroutinesDispatcherProvider: CoroutinesDispatcherProvider,
    private val webSocketRepository: WebSocketRepository,
) : FlowUseCase<DownloadBinary.DownloadBinaryParams, SocketTransfer>(coroutinesDispatcherProvider.io) {
    override fun execute(parameter: DownloadBinaryParams): Flow<Resource<SocketTransfer>> {
        return webSocketRepository.download(parameter.remotePath, parameter.outputStream).map {
            it.toResource()
        }
    }

    data class DownloadBinaryParams(val remotePath: String, val outputStream: File)
}

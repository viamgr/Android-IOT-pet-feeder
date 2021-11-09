package com.viam.feeder.domain.usecase.config

import com.viam.feeder.domain.base.CoroutinesDispatcherProvider
import com.viam.feeder.domain.base.FlowUseCase
import com.viam.feeder.domain.base.toResource
import com.viam.feeder.domain.repositories.socket.WebSocketRepository
import com.viam.websocket.model.SocketTransfer
import kotlinx.coroutines.flow.map
import java.io.InputStream
import javax.inject.Inject

class UploadBinary @Inject constructor(
    coroutinesDispatcherProvider: CoroutinesDispatcherProvider,
    private val webSocketRepository: WebSocketRepository
) : FlowUseCase<UploadBinary.UploadBinaryParams, SocketTransfer>(coroutinesDispatcherProvider.io) {
    data class UploadBinaryParams(val remotePath: String, val inputStream: InputStream)

    override fun execute(parameter: UploadBinaryParams) =
        webSocketRepository.upload(parameter.remotePath, parameter.inputStream).map {
            it.toResource()
        }
}
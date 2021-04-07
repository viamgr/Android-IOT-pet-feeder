package com.viam.feeder.data.domain.config

import com.part.livetaskcore.usecases.FlowUseCase
import com.viam.feeder.core.network.CoroutinesDispatcherProvider
import com.viam.feeder.core.utility.toResource
import com.viam.websocket.WebSocketApi
import com.viam.websocket.model.SocketTransfer
import kotlinx.coroutines.flow.map
import java.io.InputStream
import javax.inject.Inject

class UploadBinary @Inject constructor(
    coroutinesDispatcherProvider: CoroutinesDispatcherProvider,
    private val webSocketApi: WebSocketApi
) : FlowUseCase<UploadBinary.UploadBinaryParams, SocketTransfer>(coroutinesDispatcherProvider.io) {
    data class UploadBinaryParams(val remotePath: String, val inputStream: InputStream)

    override suspend fun execute(params: UploadBinaryParams) =
        webSocketApi.sendBinary(params.remotePath, params.inputStream).map {
            it.toResource()
        }
}
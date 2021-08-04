package com.viam.feeder.data.domain.event

import com.viam.feeder.core.domain.UseCase
import com.viam.feeder.core.network.CoroutinesDispatcherProvider
import com.viam.websocket.model.SocketMessage
import javax.inject.Inject


class SendEvent @Inject constructor(
    coroutinesDispatcherProvider: CoroutinesDispatcherProvider,
    private val socketApi: com.viam.websocket.WebSocketApi
) : UseCase<String, Unit>(coroutinesDispatcherProvider.io) {
    override suspend fun execute(parameters: String) {
        socketApi.sendJson(SocketMessage(parameters), SocketMessage::class.java)
    }
}
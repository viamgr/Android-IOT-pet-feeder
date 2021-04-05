package com.viam.feeder.data.domain.event

import com.viam.feeder.core.domain.UseCase
import com.viam.feeder.core.network.CoroutinesDispatcherProvider
import com.viam.feeder.socket.WebSocketApi
import com.viam.feeder.socket.model.SocketMessage
import dagger.hilt.android.scopes.ActivityScoped
import javax.inject.Inject

@ActivityScoped
class SendEvent @Inject constructor(
    coroutinesDispatcherProvider: CoroutinesDispatcherProvider,
    private val socketApi: WebSocketApi
) : UseCase<String, Unit>(coroutinesDispatcherProvider.io) {
    override suspend fun execute(parameters: String) {
        socketApi.sendJson(
            SocketMessage(parameters),
            SocketMessage::class.java
        )
    }
}
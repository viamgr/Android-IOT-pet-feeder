package com.viam.feeder.domain.usecase.event

import com.viam.feeder.domain.repositories.socket.WebSocketRepository
import com.viam.websocket.model.SocketConnectionStatus
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
class GetSyncStatus @Inject constructor(
    private val webSocketRepository: WebSocketRepository,
) {
    operator fun invoke(): SocketConnectionStatus? {
        return webSocketRepository.getSyncStatus()
    }
}
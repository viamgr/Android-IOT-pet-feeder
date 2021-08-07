package com.viam.feeder.domain.usecase.event

import com.viam.feeder.domain.base.CoroutinesDispatcherProvider
import com.viam.feeder.domain.base.UseCase
import com.viam.feeder.domain.repositories.socket.WebSocketRepository
import com.viam.feeder.model.KeyValueMessage
import javax.inject.Inject


class SendLongValue @Inject constructor(
    coroutinesDispatcherProvider: CoroutinesDispatcherProvider,
    private val webSocketRepository: WebSocketRepository
) : UseCase<KeyValueMessage<Long>, Unit>(coroutinesDispatcherProvider.io) {
    override suspend fun execute(parameters: KeyValueMessage<Long>) {
        webSocketRepository.sendLongValueMessage(
            parameters
        )
    }
}
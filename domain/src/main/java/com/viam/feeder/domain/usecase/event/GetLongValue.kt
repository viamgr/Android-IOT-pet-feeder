package com.viam.feeder.domain.usecase.event

import com.viam.feeder.domain.base.CoroutinesDispatcherProvider
import com.viam.feeder.domain.base.FlowUseCase
import com.viam.feeder.domain.repositories.socket.WebSocketRepository
import com.viam.resource.Resource
import kotlinx.coroutines.flow.map
import javax.inject.Inject


class GetLongValue @Inject constructor(
    coroutinesDispatcherProvider: CoroutinesDispatcherProvider,
    private val webSocketRepository: WebSocketRepository
) : FlowUseCase<String, Long>(coroutinesDispatcherProvider.io) {

    override fun execute(parameter: String) =
        webSocketRepository.onLongMessageReceived(
            parameter
        ).map {
            Resource.Success(it.value)
        }

}
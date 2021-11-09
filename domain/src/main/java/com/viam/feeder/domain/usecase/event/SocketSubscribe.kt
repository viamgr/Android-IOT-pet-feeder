package com.viam.feeder.domain.usecase.event

import com.viam.feeder.domain.base.CoroutinesDispatcherProvider
import com.viam.feeder.domain.base.FlowUseCase
import com.viam.feeder.domain.repositories.socket.WebSocketRepository
import com.viam.feeder.domain.repositories.system.JsonPreferences
import com.viam.resource.Resource
import com.viam.resource.Resource.Loading
import com.viam.websocket.model.SocketConnectionStatus
import com.viam.websocket.model.SocketConnectionStatus.Configured
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
class SocketSubscribe @Inject constructor(
    coroutinesDispatcherProvider: CoroutinesDispatcherProvider,
    private val webSocketRepository: WebSocketRepository,
    private val jsonPreferences: JsonPreferences,
) : FlowUseCase<Unit, SocketConnectionStatus>(coroutinesDispatcherProvider.io) {

    override fun execute(parameter: Unit): Flow<Resource<SocketConnectionStatus>> {
        return webSocketRepository.syncProcess().map {
            println("map repository $it")
            when (it) {
                is Configured -> Resource.Success(it)
                is SocketConnectionStatus.Failure -> Resource.Error(it.exception)
                else -> Loading(it)
            }
        }
    }

}
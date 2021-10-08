package com.viam.feeder.domain.usecase.event

import com.viam.feeder.domain.base.CoroutinesDispatcherProvider
import com.viam.feeder.domain.base.FlowUseCase
import com.viam.resource.Resource
import com.viam.websocket.SocketCloseException
import com.viam.websocket.WebSocketApi
import com.viam.websocket.model.SocketEvent.Closed
import com.viam.websocket.model.SocketEvent.Closing
import com.viam.websocket.model.SocketEvent.Failure
import com.viam.websocket.model.SocketEvent.Open
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class WebSocketEvents @Inject constructor(
    coroutinesDispatcherProvider: CoroutinesDispatcherProvider,
    private val webSocketApi: WebSocketApi,
) : FlowUseCase<Unit, Unit>(coroutinesDispatcherProvider.io) {
    override fun execute(parameter: Unit): Flow<Resource<Unit>> = webSocketApi.events.filter {
        it is Open || it is Failure || it is Closed || it is Closing
    }.map {
        when (it) {
            is Open -> {
                Resource.Success(Unit)
            }
            is Failure -> {
                Resource.Error(it.exception)
            }
            else -> {
                Resource.Error(SocketCloseException())
            }
        }
    }
}
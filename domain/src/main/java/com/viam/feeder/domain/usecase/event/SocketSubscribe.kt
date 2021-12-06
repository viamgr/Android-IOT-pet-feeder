package com.viam.feeder.domain.usecase.event

import com.viam.feeder.domain.base.CoroutinesDispatcherProvider
import com.viam.feeder.domain.base.FlowUseCase
import com.viam.feeder.domain.repositories.socket.DeviceRepository
import com.viam.feeder.domain.repositories.socket.WebSocketRepository
import com.viam.feeder.model.Device
import com.viam.resource.Resource
import com.viam.resource.Resource.Loading
import com.viam.websocket.model.SocketConnectionStatus
import com.viam.websocket.model.SocketConnectionStatus.Configured
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
class SocketSubscribe @Inject constructor(
    coroutinesDispatcherProvider: CoroutinesDispatcherProvider,
    private val deviceRepository: DeviceRepository,
    private val webSocketRepository: WebSocketRepository,
) : FlowUseCase<Unit, SocketConnectionStatus>(coroutinesDispatcherProvider.io) {

    override fun execute(parameter: Unit): Flow<Resource<SocketConnectionStatus>> = flow {
        val device = deviceRepository.getAll().firstOrNull() ?: Device(name = "UnknownDevice")
        emitAll(webSocketRepository.syncProcess(device.name).map {
            println("map repository $it")
            when (it) {
                is Configured -> Resource.Success(it)
                is SocketConnectionStatus.Failure -> Resource.Error(it.exception)
                is SocketConnectionStatus.Paired -> {
                    device.id = 1
                    device.name = it.deviceName
                    deviceRepository.insertAll(device)
                    Loading(it)
                }
                else -> Loading(it)
            }
        })
    }
}
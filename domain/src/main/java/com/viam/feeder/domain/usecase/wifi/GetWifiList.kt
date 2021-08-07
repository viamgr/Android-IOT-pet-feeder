package com.viam.feeder.domain.usecase.wifi

import com.viam.feeder.domain.base.CoroutinesDispatcherProvider
import com.viam.feeder.domain.base.FlowUseCase
import com.viam.feeder.domain.repositories.socket.WebSocketRepository
import com.viam.feeder.model.WifiDevice
import com.viam.resource.Resource
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject


class GetWifiList @Inject constructor(
    coroutinesDispatcherProvider: CoroutinesDispatcherProvider,
    private val webSocketRepository: WebSocketRepository
) : FlowUseCase<Unit, List<WifiDevice>>(coroutinesDispatcherProvider.io) {

    override fun execute(parameter: Unit): Flow<Resource<List<WifiDevice>>> {
        return webSocketRepository.getWifiList()
    }

}
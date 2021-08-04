package com.viam.feeder.data.domain.wifi

import com.squareup.moshi.Types
import com.viam.feeder.constants.WIFI_LIST_IS
import com.viam.feeder.core.domain.FlowUseCase
import com.viam.feeder.core.network.CoroutinesDispatcherProvider
import com.viam.feeder.data.models.KeyValueMessage
import com.viam.feeder.data.models.WifiDevice
import com.viam.resource.Resource
import com.viam.websocket.WebSocketApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject


class GetWifiList @Inject constructor(
    coroutinesDispatcherProvider: CoroutinesDispatcherProvider,
    private val webSocketApi: WebSocketApi
) : FlowUseCase<Unit, List<WifiDevice>>(coroutinesDispatcherProvider.io) {

    override fun execute(parameter: Unit): Flow<Resource.Success<List<WifiDevice>>> {
        val clazz = Types.newParameterizedType(
            KeyValueMessage::class.java, Types.newParameterizedType(
                List::class.java,
                WifiDevice::class.java
            )
        )
        return webSocketApi.onMessageReceived<KeyValueMessage<List<WifiDevice>>>(
            WIFI_LIST_IS,
            clazz
        )
            .map {
                Resource.Success(it.value)
            }
    }

}
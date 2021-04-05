package com.viam.feeder.data.domain.wifi

import com.squareup.moshi.Types
import com.viam.feeder.core.Resource
import com.viam.feeder.core.domain.FlowUseCase
import com.viam.feeder.core.network.CoroutinesDispatcherProvider
import com.viam.feeder.data.models.KeyValueMessage
import com.viam.feeder.data.models.WifiDevice
import com.viam.feeder.socket.WebSocketApi
import dagger.hilt.android.scopes.ActivityScoped
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

@ActivityScoped
class WifiList @Inject constructor(
    coroutinesDispatcherProvider: CoroutinesDispatcherProvider,
    private val webSocketApi: WebSocketApi
) : FlowUseCase<String, List<WifiDevice>>(coroutinesDispatcherProvider.io) {

    override fun execute(parameters: String): Flow<Resource.Success<List<WifiDevice>>> {
        val clazz = Types.newParameterizedType(
            KeyValueMessage::class.java, Types.newParameterizedType(
                List::class.java,
                WifiDevice::class.java
            )
        )
        return webSocketApi.onMessageReceived<KeyValueMessage<List<WifiDevice>>>(parameters, clazz)
            .map {
                Resource.Success(it.value)
            }
    }

}
package com.viam.feeder.data.domain.event

import com.squareup.moshi.Types
import com.viam.feeder.core.domain.FlowUseCase
import com.viam.feeder.core.network.CoroutinesDispatcherProvider
import com.viam.feeder.data.models.KeyValueMessage
import com.viam.resource.Resource
import com.viam.websocket.WebSocketApi
import kotlinx.coroutines.flow.map
import javax.inject.Inject


class GetLongValue @Inject constructor(
    coroutinesDispatcherProvider: CoroutinesDispatcherProvider,
    private val webSocketApi: WebSocketApi
) : FlowUseCase<String, Long>(coroutinesDispatcherProvider.io) {

    override fun execute(parameter: String) =
        webSocketApi.onMessageReceived<KeyValueMessage<Long>>(
            parameter,
            Types.newParameterizedType(KeyValueMessage::class.java, Long::class.javaObjectType)
        ).map {
            Resource.Success(it.value)
        }

}
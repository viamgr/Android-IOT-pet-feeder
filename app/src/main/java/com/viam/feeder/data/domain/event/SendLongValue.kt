package com.viam.feeder.data.domain.event

import com.squareup.moshi.Types
import com.viam.feeder.core.domain.UseCase
import com.viam.feeder.core.network.CoroutinesDispatcherProvider
import com.viam.feeder.data.models.KeyValueMessage
import com.viam.feeder.socket.WebSocketApi
import dagger.hilt.android.scopes.ActivityScoped
import javax.inject.Inject

@ActivityScoped
class SendLongValue @Inject constructor(
    coroutinesDispatcherProvider: CoroutinesDispatcherProvider,
    private val socketApi: WebSocketApi
) : UseCase<KeyValueMessage<Long>, Unit>(coroutinesDispatcherProvider.io) {
    override suspend fun execute(parameters: KeyValueMessage<Long>) {
        socketApi.sendJson(
            parameters,
            Types.newParameterizedType(KeyValueMessage::class.java, Long::class.javaObjectType)
        )
    }
}
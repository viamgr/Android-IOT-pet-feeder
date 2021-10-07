package com.viam.feeder.data.repository

import com.squareup.moshi.Types
import com.viam.feeder.domain.repositories.socket.WebSocketRepository
import com.viam.feeder.model.KeyValueMessage
import com.viam.feeder.model.WifiDevice
import com.viam.feeder.shared.WIFI_LIST_IS
import com.viam.resource.Resource
import com.viam.websocket.WebSocketApi
import com.viam.websocket.model.SocketMessage
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.io.InputStream
import java.io.OutputStream
import javax.inject.Inject

class WebSocketRepositoryImpl @Inject constructor(private val webSocketApi: WebSocketApi) :
    WebSocketRepository {
    override fun sendBinary(
        remoteFilePath: String,
        inputStream: InputStream
    ) = webSocketApi.sendBinary(remoteFilePath, inputStream)

    override fun receiveBinary(
        remoteFilePath: String,
        outputStream: OutputStream
    ) = webSocketApi.receiveBinary(remoteFilePath, outputStream)

    override fun <T : SocketMessage> onMessageReceived(key: String, clazz: Class<T>) =
        webSocketApi.onMessageReceived(key, clazz)

    override fun onLongMessageReceived(key: String): Flow<KeyValueMessage<Long>> =
        webSocketApi.onMessageReceived(
            key,
            clazz = Types.newParameterizedType(
                KeyValueMessage::class.java,
                Long::class.javaObjectType
            )
        )

    override fun sendLongValueMessage(parameters: KeyValueMessage<Long>) {
        webSocketApi.sendJson(
            parameters,
            Types.newParameterizedType(KeyValueMessage::class.java, Long::class.javaObjectType)
        )
    }

    override fun getWifiList(): Flow<Resource<List<WifiDevice>>> {
        val clazz = Types.newParameterizedType(
            KeyValueMessage::class.java, Types.newParameterizedType(
                List::class.java,
                WifiDevice::class.java
            )
        )
        return webSocketApi.onMessageReceived<KeyValueMessage<List<WifiDevice>>>(
            WIFI_LIST_IS,
            clazz = clazz
        ).map {
            Resource.Success(it.value)
        }
    }
}


package com.viam.feeder.domain.repositories.socket

import com.viam.feeder.model.KeyValueMessage
import com.viam.feeder.model.WifiDevice
import com.viam.resource.Resource
import com.viam.websocket.model.SocketMessage
import com.viam.websocket.model.SocketTransfer
import kotlinx.coroutines.flow.Flow
import java.io.InputStream
import java.io.OutputStream

interface WebSocketRepository {
    fun sendBinary(
        remoteFilePath: String,
        inputStream: InputStream,
    ): Flow<SocketTransfer>

    fun <T> sendJson(message: T, clazz: Class<T>)
    fun sendEvent(key: String)
    fun receiveBinary(
        remoteFilePath: String,
        outputStream: OutputStream
    ): Flow<SocketTransfer>

    fun <T : SocketMessage> onMessageReceived(
        key: String,
        clazz: Class<T>
    ): Flow<T>

    fun onLongMessageReceived(
        key: String,
    ): Flow<KeyValueMessage<Long>>

    fun sendLongValueMessage(parameters: KeyValueMessage<Long>)
    fun sendStringMessage(keyValue: KeyValueMessage<String>)
    fun getWifiList(): Flow<Resource<List<WifiDevice>>>
}
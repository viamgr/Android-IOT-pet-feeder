package com.viam.feeder.domain.repositories.socket

import com.viam.feeder.model.KeyValueMessage
import com.viam.feeder.model.WifiDevice
import com.viam.resource.Resource
import com.viam.websocket.model.SocketConnectionStatus
import com.viam.websocket.model.SocketEvent
import com.viam.websocket.model.SocketMessage
import com.viam.websocket.model.SocketTransfer
import kotlinx.coroutines.flow.Flow
import java.io.File
import java.io.InputStream

interface WebSocketRepository {
    fun sendBinary(remoteFilePath: String, inputStream: InputStream): Flow<SocketTransfer>

    fun sendJson(message: SocketMessage)
    fun <T> sendJson(message: T, clazz: Class<T>)
    fun sendEvent(key: String)
    fun download(remoteFilePath: String, outputFile: File): Flow<SocketTransfer>

    fun onLongMessageReceived(
        key: String,
    ): Flow<KeyValueMessage<Long>>

    fun sendLongValueMessage(parameters: KeyValueMessage<Long>)
    fun sendStringMessage(keyValue: KeyValueMessage<String>)
    fun getWifiList(): Flow<Resource<List<WifiDevice>>>

    //    fun events(): Flow<SocketEvent>
    fun subscribeAndPairAndGetConfig(deviceName: String): Flow<SocketConnectionStatus>
    fun tryToSubscribe(): Flow<SocketConnectionStatus>
    fun tryPairing(deviceName: String): Flow<SocketConnectionStatus>
    fun getEvents(): Flow<SocketEvent>
    fun syncProcess(deviceName: String): Flow<SocketConnectionStatus>
    fun upload(remoteFilePath: String, inputStream: InputStream): Flow<SocketTransfer>
    fun getSyncStatus(): SocketConnectionStatus?
}
package com.viam.feeder.data.repository

import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.viam.feeder.domain.repositories.socket.WebSocketRepository
import com.viam.feeder.model.KeyValueMessage
import com.viam.feeder.model.WifiDevice
import com.viam.feeder.model.socket.FileDetailCallback
import com.viam.feeder.model.socket.FileDetailRequest
import com.viam.feeder.model.socket.FileRequestSlice
import com.viam.feeder.shared.FILE_DETAIL_CALLBACK
import com.viam.feeder.shared.FILE_REQUEST_ERROR
import com.viam.feeder.shared.FeederConstants
import com.viam.feeder.shared.PAIR
import com.viam.feeder.shared.PAIR_DONE
import com.viam.feeder.shared.PAIR_ERROR
import com.viam.feeder.shared.SUBSCRIBE
import com.viam.feeder.shared.SUBSCRIBE_DONE
import com.viam.feeder.shared.SUBSCRIBE_ERROR
import com.viam.feeder.shared.WIFI_LIST_IS
import com.viam.resource.Resource
import com.viam.websocket.WebSocketApi
import com.viam.websocket.checkHasError
import com.viam.websocket.model.SocketConnectionStatus
import com.viam.websocket.model.SocketConnectionStatus.Configured
import com.viam.websocket.model.SocketConnectionStatus.Configuring
import com.viam.websocket.model.SocketConnectionStatus.Paired
import com.viam.websocket.model.SocketConnectionStatus.Pairing
import com.viam.websocket.model.SocketConnectionStatus.Subscribed
import com.viam.websocket.model.SocketConnectionStatus.Subscribing
import com.viam.websocket.model.SocketEvent
import com.viam.websocket.model.SocketEvent.Binary
import com.viam.websocket.model.SocketEvent.Open
import com.viam.websocket.model.SocketEvent.Text
import com.viam.websocket.model.SocketMessage
import com.viam.websocket.model.SocketTransfer
import com.viam.websocket.model.SocketTransfer.Progress
import com.viam.websocket.model.SocketTransfer.Start
import com.viam.websocket.model.SocketTransfer.Success
import com.viam.websocket.model.TransferType.Download
import com.viam.websocket.sendEventWithCallbackCheck
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flatMapMerge
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.produceIn
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.io.FileOutputStream
import java.io.InputStream
import java.io.OutputStream
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WebSocketRepositoryImpl @Inject constructor(
    private val webSocketApi: WebSocketApi,
    val moshi: Moshi
) : WebSocketRepository {
    private val downloadingMutex = Mutex()

    override fun getEvents(): Flow<SocketEvent> = webSocketApi.events
    override fun sendBinary(
        remoteFilePath: String,
        inputStream: InputStream
    ) = webSocketApi.sendBinary(remoteFilePath, inputStream)

    override fun sendJson(message: SocketMessage) {
        webSocketApi.sendParametericMessage(message)
    }

    override fun <T> sendJson(message: T, clazz: Class<T>) {
        webSocketApi.sendJson(message, clazz)
    }

    override fun sendEvent(key: String) {
        webSocketApi.sendJson(
            SocketMessage(key),
            SocketMessage::class.java
        )
    }

    override fun subscribeAndPair(fileOutputStream: FileOutputStream): Flow<SocketConnectionStatus> =
        channelFlow {
            var isSubscribing = false
//            val triggers = merge(flowOf(Unit), getEvents().filterIsInstance<Open>())
            val triggers = getEvents().filterIsInstance<Open>()
            // TODO: 11/1/2021 fix triggers
            triggers
                .filter {
                    !isSubscribing
                }
                .flatMapLatest {
                    isSubscribing = true
                    tryToSubscribe()
                }
                .onEach { send(it) }
                .filter {
                    it is Subscribed
                }
                .flatMapLatest {
                    tryPairing()
                }
                .filter {
                    send(it)
                    it is Paired
                }
                .flatMapMerge {
                    getConfigs(fileOutputStream)
                }
                .onEach {
                    when (it) {
                        is Start -> send(Configuring(0F))
                        is Progress -> send(Configuring(it.progress))
                        is Success -> send(Configured)
                        is SocketTransfer.Error -> send(SocketConnectionStatus.Failure(it.exception))

                    }
                }
                .onCompletion {
                    isSubscribing = false
                }
                .catch { e ->
                    send(SocketConnectionStatus.Failure(e as Exception))
                }
                .collect()

        }

    override fun download(
        remoteFilePath: String, outputStream: OutputStream
    ) = flow {
        emit(Progress(0F))
        downloadingMutex.withLock {
            val size = requestGetDetailAndWaitForCallback(remoteFilePath).let {
                emit(Start(it.size, Download))
                it.size
            }

            var wrote = 0
            do {
                wrote += downloadAndWrite(wrote, outputStream)
                emit(Progress(wrote.toFloat()))
            } while (wrote < size)

            emit(Success)
        }
    }

    private suspend fun downloadAndWrite(
        from: Int,
        outputStream: OutputStream
    ): Int {
        val slice = sendSliceRequestAndWaitForCallback(from)
        val toByteArray = slice.data.toByteArray()
        outputStream.write(toByteArray)
        return toByteArray.size
    }

    private suspend fun requestGetDetailAndWaitForCallback(remoteFilePath: String): FileDetailCallback {
        val channel = receiveChannel()

        requestDetail(remoteFilePath)

        val callback =
            channel.sendEventWithCallbackCheck(FILE_DETAIL_CALLBACK, FILE_REQUEST_ERROR) as Text
        return moshi.adapter(FileDetailCallback::class.java).fromJson(callback.data)!!
    }

    private fun requestDetail(remoteFilePath: String) {
        webSocketApi.sendParametericMessage(FileDetailRequest(remoteFilePath))
    }

    private suspend fun sendSliceRequestAndWaitForCallback(
        from: Int
    ): Binary {
        val channel = receiveChannel()
        println("request $from")
        requestReceiveFile(from)

        return channel
            .sendEventWithCallbackCheck(1000) {
                it.checkHasError(FILE_REQUEST_ERROR)
                it.isBinary()
            } as Binary
    }

    private fun requestReceiveFile(from: Int) {
        webSocketApi.sendParametericMessage(FileRequestSlice(from))
    }

    private fun SocketEvent.isBinary() = this is Binary

    private fun getConfigs(fileOutputStream: FileOutputStream): Flow<SocketTransfer> {
        return download("/${FeederConstants.CONFIG_FILE_PATH}", fileOutputStream)
    }

    override fun tryPairing(): Flow<SocketConnectionStatus> = flow {
        val channel = receiveChannel()
        emit(Pairing)
        sendPairMessage()
        channel.sendEventWithCallbackCheck(PAIR_DONE, PAIR_ERROR)
        emit(Paired)
    }

    override fun tryToSubscribe(): Flow<SocketConnectionStatus> =
        flow {
            emit(Subscribing)
            sendSubscribeMessage()
            receiveChannel().sendEventWithCallbackCheck(SUBSCRIBE_DONE, SUBSCRIBE_ERROR)
            emit(Subscribed)
        }

    private suspend fun receiveChannel() = getEvents().produceIn(CoroutineScope(currentCoroutineContext()))

    private fun sendPairMessage() {
        // TODO: 11/1/2021 get Feeder1 from other place

        val newParameterizedType = Types.newParameterizedType(
            KeyValueMessage::class.java,
            String::class.java
        )
        webSocketApi.sendJson(
            KeyValueMessage(PAIR, "Feeder1"),
            newParameterizedType
        )
    }

    private fun sendSubscribeMessage() {
        webSocketApi.sendJson(SocketMessage(SUBSCRIBE))
    }

    override fun onLongMessageReceived(key: String): Flow<KeyValueMessage<Long>> =
        webSocketApi.waitForMessage(
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

    override fun sendStringMessage(keyValue: KeyValueMessage<String>) {
        webSocketApi.sendJson(
            keyValue,
            Types.newParameterizedType(KeyValueMessage::class.java, String::class.javaObjectType)
        )
    }

    override fun getWifiList(): Flow<Resource<List<WifiDevice>>> {
        val clazz = Types.newParameterizedType(
            KeyValueMessage::class.java, Types.newParameterizedType(
                List::class.java,
                WifiDevice::class.java
            )
        )
        return webSocketApi.waitForMessage<KeyValueMessage<List<WifiDevice>>>(
            WIFI_LIST_IS,
            clazz = clazz
        ).map {
            Resource.Success(it.value)
        }
    }
}


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
import com.viam.feeder.shared.UNPAIR
import com.viam.feeder.shared.UNSUBSCRIBE
import com.viam.feeder.shared.WIFI_LIST_IS
import com.viam.resource.Resource
import com.viam.websocket.SocketCloseException
import com.viam.websocket.WebSocketApi
import com.viam.websocket.checkHasError
import com.viam.websocket.containsKey
import com.viam.websocket.model.SocketConnectionStatus
import com.viam.websocket.model.SocketConnectionStatus.Configured
import com.viam.websocket.model.SocketConnectionStatus.Configuring
import com.viam.websocket.model.SocketConnectionStatus.Paired
import com.viam.websocket.model.SocketConnectionStatus.Pairing
import com.viam.websocket.model.SocketConnectionStatus.Subscribed
import com.viam.websocket.model.SocketConnectionStatus.Subscribing
import com.viam.websocket.model.SocketEvent
import com.viam.websocket.model.SocketEvent.Binary
import com.viam.websocket.model.SocketEvent.Closed
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
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flatMapMerge
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.produceIn
import kotlinx.coroutines.flow.retryWhen
import kotlinx.coroutines.flow.single
import kotlinx.coroutines.flow.transform
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.io.FileOutputStream
import java.io.InputStream
import java.io.OutputStream
import java.util.concurrent.TimeoutException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WebSocketRepositoryImpl @Inject constructor(
    private val webSocketApi: WebSocketApi,
    val moshi: Moshi
) : WebSocketRepository {
    private val downloadingMutex = Mutex()

    override fun getEvents() = webSocketApi.events
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

    override fun syncProcess(fileOutputStream: FileOutputStream): Flow<SocketConnectionStatus> = channelFlow {
        getEvents().transform {
            println("transform $it")
            if (it is Closed || (it is Text && (it.containsKey(UNPAIR) || it.containsKey(UNSUBSCRIBE)))
            ) {
                emit(SocketConnectionStatus.Failure(SocketCloseException()))
            } else if (it is Open) {
                emitAll(subscribeAndPairAndGetConfig(fileOutputStream))
            } else if (it is Text && it.containsKey(PAIR_DONE)) {
                emitAll(getConfigs(fileOutputStream).map { it.toConnectionStatus() })
            }
        }.retryWhen { cause, attempt ->
            emit(SocketConnectionStatus.Failure(cause as Exception))
            println("retrying...")
            true
        }.collect {
            println("collect $it")
            send(it)
        }
    }

    override fun subscribeAndPairAndGetConfig(fileOutputStream: FileOutputStream): Flow<SocketConnectionStatus> =
        channelFlow {
            /*   val context = currentCoroutineContext()
               async {
                   getEvents().collect {
                       if (it is Closed ||
                           (it is Text && (it.containsKey(UNPAIR) || it.containsKey(UNSUBSCRIBE)))
                       ) {
                           send(SocketConnectionStatus.Failure(SocketCloseException()))
                           context.cancel()
                       }
                   }

               }*/
            println("subscribeAndPair")

            tryToSubscribe()
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
                    send(it.toConnectionStatus())
                }
                .collect()
        }

    fun SocketTransfer.toConnectionStatus(): SocketConnectionStatus {
        return when (this) {
            is Start -> Configuring(0F)
            is Progress -> Configuring(progress)
            is Success -> Configured
            is SocketTransfer.Error -> SocketConnectionStatus.Failure(exception)
        }
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
        emit(Pairing)
        sendPairMessage()
        println("start getting pair done")
        receiveChannel().sendEventWithCallbackCheck(PAIR_DONE, PAIR_ERROR)
        emit(Paired)
    }

    suspend fun wait(successKey: String, errorKey: String) = coroutineScope {

        async {
            delay(5000)
            println("timeout in pair")
            throw TimeoutException(successKey)
        }
        async {
            val a = getEvents().filter {
                println("filter in event $it ${it.containsKey(successKey)}")
                it.checkHasError(errorKey)
                it.containsKey(successKey)
            }.single()
            println("aaaaaaaa: $a")
            a
        }

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


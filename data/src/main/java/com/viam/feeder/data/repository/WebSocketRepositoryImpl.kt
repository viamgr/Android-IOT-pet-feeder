package com.viam.feeder.data.repository

import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.viam.feeder.domain.repositories.socket.WebSocketRepository
import com.viam.feeder.domain.repositories.system.JsonPreferences
import com.viam.feeder.model.KeyValueMessage
import com.viam.feeder.model.WifiDevice
import com.viam.feeder.model.socket.*
import com.viam.feeder.shared.*
import com.viam.resource.Resource
import com.viam.websocket.*
import com.viam.websocket.model.*
import com.viam.websocket.model.SocketConnectionStatus.*
import com.viam.websocket.model.SocketEvent.*
import com.viam.websocket.model.SocketEvent.Failure
import com.viam.websocket.model.SocketTransfer.*
import com.viam.websocket.model.TransferType.Download
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import okio.ByteString.Companion.toByteString
import org.json.JSONObject
import java.io.BufferedInputStream
import java.io.File
import java.io.InputStream
import java.io.OutputStream
import java.util.concurrent.TimeoutException
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton

@Singleton
class WebSocketRepositoryImpl @Inject constructor(
    private val webSocketApi: WebSocketApi,
    val moshi: Moshi,
    private val jsonPreferences: JsonPreferences,
    @Named("configFile") private val configFile: File
) : WebSocketRepository {
    private val downloadingMutex = Mutex()
    private val uploadingMutex = Mutex()

    override fun getEvents() = webSocketApi.events
    override fun sendBinary(
        remoteFilePath: String,
        inputStream: InputStream
    ) = upload(remoteFilePath, inputStream)

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

    override fun syncProcess(): Flow<SocketConnectionStatus> = channelFlow {

        var syncJon: Job? = null
        var configJob: Job? = null

        val invokeSync: (suspend () -> Unit) = {

            if (configJob?.isActive != true) {
                syncJon?.cancel()
                syncJon = async {
                    subscribeAndPairAndGetConfig().catch { e ->
                        send(SocketConnectionStatus.Failure(e as Exception))
                    }.collect {
                        send(it)
                    }
                }
            }
        }
        val invokeConfig: (suspend () -> Unit) = {
            if (syncJon?.isActive != true) {
                configJob?.cancel()
                configJob = async {
                    getConfigs().map { it.toConnectionStatus() }.catch { e ->
                        send(SocketConnectionStatus.Failure(e as Exception))
                    }.collect {
                        send(it)
                    }
                }
            }
        }
        getEvents().collect {
            println("collected data in sync progress: $it")
            try {
                if (hasErrorInSync(it)) {
                    send(SocketConnectionStatus.Failure(SocketCloseException("error in sync")))
                    syncJon?.cancel()
                    configJob?.cancel()
                } else if (it is Open) {
                    invokeSync()
                } else if (it is Text && it.containsKey(PAIR_DONE)) {
                    invokeConfig()
                }
            } catch (e: Exception) {
                send(SocketConnectionStatus.Failure(e))
            }
        }
    }

    private fun hasErrorInSync(it: SocketEvent) =
        it is Closed || it is Failure || (it is Text && (it.containsKey(UNPAIR) || it.containsKey(
            UNSUBSCRIBE
        )))

    override fun subscribeAndPairAndGetConfig(): Flow<SocketConnectionStatus> =
        channelFlow {
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
                    getConfigs()
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

    override fun upload(
        remoteFilePath: String, inputStream: InputStream
    ) = flow {
        emit(Progress(0F))
        uploadingMutex.withLock {
            val size = inputStream.available()
            println("upload size:$size")

            var buffered: BufferedInputStream? = null
            emit(Start(size, TransferType.Upload))

            val channel = receiveChannel()
            webSocketApi.sendParametericMessage(SendFileMessage(remoteFilePath, size))

            try {
                do {
                    val sliceMessageCallback = try {
                        channel
                            .waitForCallback(takeWhile = {
                                it.checkHasError(FILE_SEND_ERROR)
                                it.containsKey(FILE_SEND_SLICE) || it.containsKey(FILE_SEND_FINISHED)
                            }) as Text
                    } catch (e: TimeoutException) {
                        throw TimeoutException("timeout in sending binary data")
                    } catch (e: Exception) {
                        throw e
                    }

                    if (sliceMessageCallback.containsKey(FILE_SEND_FINISHED)) {
                        break
                    }

                    val receiveSliceMessage =
                        moshi.adapter(ReceiveSliceMessage::class.java)
                            .fromJson(sliceMessageCallback.data)!!

                    val start = receiveSliceMessage.start
                    val end = receiveSliceMessage.end
                    val offset = end - start
                    if (buffered == null)
                        buffered = inputStream.buffered(offset)
                    val buff = ByteArray(offset)
                    buffered.read(buff)
                    emit(Progress(start.toFloat() / end))
                    val message = buff.toByteString(0, offset)

                    println("upload offset:$offset")
                    println("upload message:$message")
                    webSocketApi.sendByteString(message)
                    delay(5000)
                } while (currentCoroutineContext().isActive)

                emit(Success)
            } catch (e: Exception) {
                throw e
            } finally {
                inputStream.close()
            }
        }
    }

    override fun download(remoteFilePath: String, outputFile: File): Flow<SocketTransfer> = flow {
        emit(Progress(0F))
        downloadingMutex.withLock {
            val size = requestGetDetailAndWaitForCallback(remoteFilePath).let {
                emit(Start(it.size, Download))
                it.size
            }

            val outputStream = configFile.outputStream()
            try {
                var wrote = 0
                do {
                    wrote += downloadAndWrite(wrote, outputStream)
                    emit(Progress(wrote.toFloat()))
                } while (wrote < size && currentCoroutineContext().isActive)

                emit(Success)
            } catch (e: Exception) {
                throw e
            } finally {
                outputStream.close()
            }
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
            channel.waitForCallback(FILE_DETAIL_CALLBACK, FILE_REQUEST_ERROR) as Text
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

        val binary = try {
            channel
                .waitForCallback(takeWhile = { it ->
                    it.checkHasError(FILE_REQUEST_ERROR)
                    it.isBinary()
                }) as Binary
        } catch (e: TimeoutException) {
            throw TimeoutException("timeout in getting binary data")
        } catch (e: Exception) {
            throw e
        }
        return binary
    }

    private fun requestReceiveFile(from: Int) {
        webSocketApi.sendParametericMessage(FileRequestSlice(from))
    }

    private fun SocketEvent.isBinary() = this is Binary

    private fun getConfigs(): Flow<SocketTransfer> {
        return download("/${FeederConstants.CONFIG_FILE_PATH}", configFile).also {
            configFile.readText().let {
                val jsonObject = try {
                    JSONObject(it)
                } catch (e: Exception) {
                    JSONObject()
                }
                jsonPreferences.storeJson(jsonObject)
            }
        }
    }

    override fun tryPairing(): Flow<SocketConnectionStatus> = flow {
        emit(Pairing)
        sendPairMessage()
        println("start getting pair done")
        receiveChannel().waitForCallback(PAIR_DONE, PAIR_ERROR)
        emit(Paired)
    }

    /* suspend fun wait(successKey: String, errorKey: String) = coroutineScope {

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
             a
         }

     }*/

    override fun tryToSubscribe(): Flow<SocketConnectionStatus> =
        flow {
            emit(Subscribing)
            sendSubscribeMessage()
            receiveChannel().waitForCallback(SUBSCRIBE_DONE, SUBSCRIBE_ERROR)
            emit(Subscribed)
        }

    private suspend fun receiveChannel() =
        getEvents().produceIn(CoroutineScope(currentCoroutineContext()))

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


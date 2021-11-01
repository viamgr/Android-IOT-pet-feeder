package com.viam.websocket

import com.squareup.moshi.Moshi
import com.viam.websocket.model.*
import com.viam.websocket.model.SocketEvent.Binary
import com.viam.websocket.model.SocketEvent.Closed
import com.viam.websocket.model.SocketEvent.Closing
import com.viam.websocket.model.SocketEvent.Failure
import com.viam.websocket.model.SocketEvent.Open
import com.viam.websocket.model.SocketEvent.Text
import com.viam.websocket.model.SocketTransfer.Error
import kotlinx.coroutines.*
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.flow.*
import okhttp3.*
import okio.ByteString
import java.io.BufferedInputStream
import java.io.InputStream
import java.lang.reflect.ParameterizedType
import kotlin.coroutines.CoroutineContext

typealias ConnectionListener = (Boolean) -> Unit

@Suppress("BlockingMethodInNonBlockingContext")
class WebSocketApi(
    val okHttpClient: OkHttpClient,
    val moshi: Moshi
) {
    private var connectionListener: ConnectionListener? = null
    private var isOpenedSocket = false
        set(value) {
            field = value
            if (!value) {
                cancelOldBinaryTransfer()
            }
            connectionListener?.invoke(value)
        }

    private val _events = MutableSharedFlow<SocketEvent>() // private mutable shared flow
    private val _progress =
        MutableSharedFlow<SocketTransfer>() // private mutable shared flow
    private var binaryCoroutineContext: CoroutineContext? = null

    private val errorListeners = mutableListOf<(e: Exception) -> Unit>()
    val events: Flow<SocketEvent> = _events.asSharedFlow() // publicly exposed as read-only shared flow
    val progress = _progress.asSharedFlow() // publicly exposed as read-only shared flow

    private val socketRunnerScope = CoroutineScope(IO)
    lateinit var webSocket: WebSocket
    fun launchInScope(block: suspend () -> Unit): Job {
        return socketRunnerScope.launch {
            try {
                println("new Launch")
                block.invoke()
            } catch (e: Exception) {
                e.printStackTrace()
                onErrorHappened(e)
            }
        }
    }

    @Suppress("UNCHECKED_CAST")
    inline fun <reified T : SocketEvent> onEvent(
        crossinline callback: (data: T) -> Unit
    ) {
        launchInScope {
            events
                .filterIsInstance<T>()
                .collect {
                    callback.invoke(it)
                }
        }
    }

    @Suppress("UNCHECKED_CAST")
    fun <T : SocketMessage> waitForMessage(
        key: String,
        clazz: ParameterizedType
    ): Flow<T> = channelFlow {
        if (!isOpenedSocket) {
            throw SocketCloseException()
        }
        events
            .filterIsInstance<Text>()
            .map {
                val socketMessage = moshi.adapter(SocketMessage::class.java).fromJson(it.data)
                return@map if (socketMessage?.key == key) {
                    return@map moshi.adapter<T>(clazz).fromJson(it.data) as T
                } else null
            }
            .filterNotNull().collect {
                send(it)
            }
    }

    @Suppress("UNCHECKED_CAST")
    fun <T : SocketMessage> waitForMessage(
        key: String,
    ): Flow<T> = flow {
        /* events
             .filterIsInstance<Text>()
             .map {
                 val socketMessage = moshi.adapter(SocketMessage::class.java).fromJson(it.data)
                 println("waitForMessage ${socketMessage?.key}")

                 return@map if (socketMessage?.key == key) {
                     return@map moshi.adapter(clazz).fromJson(it.data) as T
                 } else null
             }
             .filterNotNull().onEach {
                 println("waitForMessage filterNotNull")
                 timeout.cancel()
             }
             .catch { e ->
                 println("waitForMessage catch")
                 e.printStackTrace()

                 throw e
             }
             .collect {
                 println("waitForMessage collectcollectcollect $it")
                 emit(it)
             }*/
    }

    fun openWebSocket(request: Request) {
        if (isOpenedSocket) {
            return
        }
        webSocket = okHttpClient.newWebSocket(request, object : WebSocketListener() {
            override fun onOpen(webSocket: WebSocket, response: Response) {
                super.onOpen(webSocket, response)
                println("onOpen socket")
                isOpenedSocket = true
                launchInScope {
                    _events.emit(Open)
                }
            }

            override fun onMessage(webSocket: WebSocket, text: String) {
                super.onMessage(webSocket, text)
                println("onMessage socket $text")
                launchInScope {
                    _events.emit(Text(text))
                }
            }

            override fun onMessage(webSocket: WebSocket, bytes: ByteString) {
                super.onMessage(webSocket, bytes)
                println("onMessage socket $bytes")
                launchInScope {
                    _events.emit(Binary(bytes))
                }
            }

            override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
                super.onClosing(webSocket, code, reason)
                isOpenedSocket = false
                println("onClosing socket")
                launchInScope {
                    _events.emit(Closing)
                }
            }

            override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
                super.onClosed(webSocket, code, reason)
                isOpenedSocket = false
                println("onClosed socket")
                launchInScope {
                    _events.emit(Closed)
                }
            }

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                super.onFailure(webSocket, t, response)
                println("onFailure socket")
                isOpenedSocket = false
                okHttpClient.dispatcher.cancelAll()
                okHttpClient.connectionPool.evictAll()
                webSocket.cancel()
                webSocket.close(1000, "Connection closed")

                launchInScope {
                    _events.emit(Failure(Exception(t)))
                    delay(5000)
                    openWebSocket(request)
                }
            }
        })
    }

    private var buffered: BufferedInputStream? = null

    @OptIn(ExperimentalCoroutinesApi::class)
    fun sendBinary(
        remoteFilePath: String,
        inputStream: InputStream,
    ): Flow<SocketTransfer> {
        /*var timeout: Job? = null
        if (buffered != null) {
            throw IllegalStateException()
        }

        fun close() {
            buffered?.close()
            buffered = null
        }

        fun cancelTimeout() {
            timeout?.cancel()
            timeout = null
        }

        suspend fun renewTimeout() {
            cancelTimeout()
            timeout = coroutineScope {
                launch {
                    delay(5000)
                    throw TimeoutException()
                }
            }
        }

        val startFlow: Flow<SocketTransfer> = flow {
            val size = inputStream.available()
            sendJson(SendFileMessage(remoteFilePath, size), SendFileMessage::class.java)
            emit(Start(size, TransferType.Upload))
            renewTimeout()
        }

        val messagesFlow = events
            .filterIsInstance<Text>()
            .map {
                val socketMessage = moshi.adapter(SocketMessage::class.java).fromJson(it.data)!!
                println("socketMessage.key: ${socketMessage.key}")
                when (socketMessage.key) {
                    FILE_SEND_SLICE -> {
                        cancelTimeout()

                        val sliceMessage = moshi.adapter(ReceiveSliceMessage::class.java).fromJson(it.data)!!
                        val start = sliceMessage.start
                        val end = sliceMessage.end
                        val offset = end - start
                        if (buffered == null)
                            buffered = inputStream.buffered(offset)
                        val buff = ByteArray(offset)
                        buffered?.read(buff)
                        sendByteString(buff.toByteString(0, offset))
                        renewTimeout()
                        Progress(start.toFloat() / end)
                    }
                    FILE_SEND_FINISHED -> {
                        cancelTimeout()
                        Success
                    }
                    FILE_REQUEST_ERROR -> {
                        cancelTimeout()
                        Error(Exception())
                    }
                    else -> null
                }
            }

        return startFlow
            .flatMapLatest {
                messagesFlow
            }
            .filterNotNull()
            .onEach {
                if (it is Error || it is Success) {
                    close()
                }
            }*/
        return flowOf()
    }

    fun sendByteString(message: ByteString) {
        println("send binary ${message.size}")
        webSocket.send(message)
    }

    fun <T> sendJson(message: T, clazz: ParameterizedType): WebSocketApi {
        val toJson = moshi.adapter<T>(clazz).toJson(message)
        sendMessage(toJson)
        return this
    }

    fun <T> sendJson(message: T, clazz: Class<T>): WebSocketApi {
        val toJson = moshi.adapter(clazz).toJson(message)
        sendMessage(toJson)
        return this
    }

    inline fun <reified T> sendParametericMessage(message: T): WebSocketApi {
        val toJson = moshi.adapter(T::class.java).toJson(message)
        sendMessage(toJson)
        return this
    }

    fun sendJson(message: SocketMessage): WebSocketApi {
        return sendJson(message, SocketMessage::class.java)
    }

    fun setOnConnectionChangedListener(listener: ConnectionListener) {
        this.connectionListener = listener
    }

    fun sendMessage(toJson: String): Boolean {
        println("send text $toJson")

        println("isOpened:$isOpenedSocket")
        return when {
            !isOpenedSocket -> {
                throw SocketCloseException()
            }
            webSocket.send(toJson) -> {
                true
            }
            else -> {
                throw FailedToSendException()
            }
        }
    }

    private fun cancelOldBinaryTransfer() {
        if (binaryCoroutineContext != null && binaryCoroutineContext?.isActive == true)
            binaryCoroutineContext?.cancel()
    }

    private fun Flow<SocketTransfer>.handleErrors(): Flow<SocketTransfer> = flow {
        try {
            collect { value ->
                emit(value)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            emit(Error(e))
        }
    }

    private fun onErrorHappened(e: Exception) {
        println("errorListeners: ${errorListeners.size}")
        errorListeners.forEach {
            it.invoke(e)
        }
    }
}
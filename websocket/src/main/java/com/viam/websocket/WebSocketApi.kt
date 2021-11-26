package com.viam.websocket

import com.squareup.moshi.Moshi
import com.viam.websocket.model.*
import com.viam.websocket.model.SocketEvent.*
import com.viam.websocket.model.SocketTransfer.Error
import kotlinx.coroutines.*
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.flow.*
import okhttp3.*
import okio.ByteString
import java.io.BufferedInputStream
import java.lang.reflect.ParameterizedType
import kotlin.coroutines.CoroutineContext

typealias ConnectionListener = (Boolean) -> Unit

@Suppress("BlockingMethodInNonBlockingContext")
class WebSocketApi(
    val okHttpClient: OkHttpClient,
    val moshi: Moshi
) {
    private var connectionListener: ConnectionListener? = null
    private var lastRetry = 0L
    private var isOpenedSocket = false
        set(value) {
            field = value
            if (!value) {
                cancelOldBinaryTransfer()
            }
            connectionListener?.invoke(value)
        }

    private val _events =
        MutableSharedFlow<SocketEvent>() // private mutable shared flow
    private val _progress =
        MutableSharedFlow<SocketTransfer>() // private mutable shared flow
    private var binaryCoroutineContext: CoroutineContext? = null

    private val errorListeners = mutableListOf<(e: Exception) -> Unit>()
    val events: SharedFlow<SocketEvent> = _events // publicly exposed as read-only shared flow
    val progress = _progress.asSharedFlow() // publicly exposed as read-only shared flow

    private val socketRunnerScope = CoroutineScope(IO)
    var webSocket: WebSocket? = null
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
                val string = String(bytes.toByteArray())
                println("onMessage socket $string")
                launchInScope {
                    _events.emit(Binary(bytes))
                }
            }

            override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
                super.onClosing(webSocket, code, reason)
                isOpenedSocket = false
                println("onClosing socket code:$code $reason")
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

            override fun onFailure(socket: WebSocket, t: Throwable, response: Response?) {
                super.onFailure(socket, t, response)
                synchronized(lastRetry) {
                    if (System.currentTimeMillis() - lastRetry > 4000) {

                        lastRetry = System.currentTimeMillis()
                        println("onFailure socket $t")
                        isOpenedSocket = false

                        socket.close(1000, null)
                        webSocket?.close(1000, "AAAAAAAAAAAAAAAAAAAAAAAAAAAA")

                        okHttpClient.dispatcher.cancelAll()
                        okHttpClient.connectionPool.evictAll()
                        webSocket = null

                        launchInScope {
                            _events.emit(Failure(Exception(t)))
                            delay(5000)
                            openWebSocket(request)
                        }
                    }

                }
            }
        })
    }

    private var buffered: BufferedInputStream? = null

    fun sendByteString(message: ByteString) {
        println("send binary $message")
        webSocket?.send(message)
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
                throw SocketCloseException("Socket is not open")
            }
            webSocket?.send(toJson) == true -> {
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

    fun isOpen() = isOpenedSocket
}
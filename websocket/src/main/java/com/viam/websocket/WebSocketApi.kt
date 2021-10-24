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
import com.viam.websocket.model.SocketTransfer.Progress
import com.viam.websocket.model.SocketTransfer.Start
import com.viam.websocket.model.SocketTransfer.Success
import kotlinx.coroutines.*
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.flow.*
import okhttp3.*
import okio.ByteString
import okio.ByteString.Companion.toByteString
import java.io.BufferedInputStream
import java.io.InputStream
import java.io.OutputStream
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
    fun launch(block: suspend () -> Unit): Job {
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
    suspend fun onBinaryReceived(): Flow<SocketEvent.Binary> = channelFlow {
        events
            .filterIsInstance<SocketEvent.Binary>()
            .collect {
                send(it)
            }
    }

    @Suppress("UNCHECKED_CAST")
    inline fun <reified T : SocketEvent> onEvent(
        crossinline callback: (data: T) -> Unit
    ) {
        launch {
            events
                .filterIsInstance<T>()
                .collect {
                    callback.invoke(it)
                }
        }
    }

    @Suppress("UNCHECKED_CAST")
    fun <T : SocketMessage> onMessageReceived(
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
    fun <T : SocketMessage> onMessageReceived(
        key: String,
        clazz: Class<T>
    ): Flow<T> {
        return events
            .filterIsInstance<Text>()
            .map {
                val socketMessage = moshi.adapter(SocketMessage::class.java).fromJson(it.data)
                return@map if (socketMessage?.key == key) {
                    return@map moshi.adapter(clazz).fromJson(it.data) as T
                } else null
            }
            .filterNotNull()
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
                launch {
                    _events.emit(Open)
                }
            }

            override fun onMessage(webSocket: WebSocket, text: String) {
                super.onMessage(webSocket, text)
                println("onMessage socket $text")
                launch {
                    _events.emit(Text(text))
                }
            }

            override fun onMessage(webSocket: WebSocket, bytes: ByteString) {
                super.onMessage(webSocket, bytes)
                println("onMessage socket $bytes")
                launch {
                    _events.emit(Binary(bytes))
                }
            }

            override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
                super.onClosing(webSocket, code, reason)
                isOpenedSocket = false
                println("onClosing socket")
                launch {
                    _events.emit(Closing)
                }
            }

            override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
                super.onClosed(webSocket, code, reason)
                isOpenedSocket = false
                println("onClosed socket")
                launch {
                    _events.emit(Closed)
                }
            }

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                super.onFailure(webSocket, t, response)
                println("onFailure socket")
                isOpenedSocket = false
                okHttpClient.dispatcher.cancelAll()
                okHttpClient.connectionPool.evictAll();
                webSocket.cancel()
                webSocket.close(1000, "Connection closed");

//                response?.body?.close()
//                webSocket.cancel()
                launch {
                    _events.emit(Failure(Exception(t)))
                    delay(5000)
                    openWebSocket(request)
                }
            }
        })
    }

    @Suppress("BlockingMethodInNonBlockingContext")
    fun receiveBinary(
        remoteFilePath: String,
        outputStream: OutputStream
    ): Flow<SocketTransfer> = channelFlow {
        println("Start Download: $remoteFilePath")

        var wrote = 0
        var size = 0

        sendJson(FileDetailRequest(remoteFilePath), FileDetailRequest::class.java)
        send(Progress(0F))

        events.collect {
            if (it is Text) {
                val socketMessage = moshi.adapter(SocketMessage::class.java).fromJson(it.data)!!
                println("socketMessage.key: ${socketMessage.key}")
                when (socketMessage.key) {
                    FILE_DETAIL_CALLBACK -> {
                        val detailCallback =
                            moshi.adapter(FileDetailCallback::class.java).fromJson(it.data)!!
                        size = detailCallback.size
                        sendJson(FileRequestSlice(wrote), FileRequestSlice::class.java)
                        send(Start(size, TransferType.Download))
                    }
                    FILE_SEND_ERROR -> {
                        val detailCallback =
                            moshi.adapter(FileDetailCallback::class.java).fromJson(it.data)!!
                        size = detailCallback.size
                        send(Error(Exception(FILE_REQUEST_ERROR)))
                    }
                }
            } else if (it is Binary) {
                val toByteArray = it.data.toByteArray()
                outputStream.write(toByteArray)
                wrote += toByteArray.size
                if (wrote < size) {
                    sendJson(FileRequestSlice(wrote), FileRequestSlice::class.java)
                    send(Progress(wrote.toFloat() / size))
                } else {
                    sendJson(SocketMessage(FILE_REQUEST_FINISHED), SocketMessage::class.java)
                    send(Progress(1F))
                    send(Success)
                }
            }
        }
    }

    var buffered: BufferedInputStream? = null

    @OptIn(ExperimentalCoroutinesApi::class)
    fun sendBinary(
        remoteFilePath: String,
        inputStream: InputStream,
    ): Flow<SocketTransfer> {
        if (buffered != null) {
            throw IllegalStateException()
        }

        fun close() {
            buffered?.close()
            buffered = null
        }

        val startFlow: Flow<SocketTransfer> = flow {
            val size = inputStream.available()
            sendJson(SendFileMessage(remoteFilePath, size), SendFileMessage::class.java)
            emit(Start(size, TransferType.Upload))
        }

        val messagesFlow = events
            .filterIsInstance<Text>().map {
                val socketMessage = moshi.adapter(SocketMessage::class.java).fromJson(it.data)!!
                println("socketMessage.key: ${socketMessage.key}")
                when (socketMessage.key) {
                    FILE_SEND_SLICE -> {
                        val sliceMessage = moshi.adapter(ReceiveSliceMessage::class.java).fromJson(it.data)!!

                        val start = sliceMessage.start
                        val end = sliceMessage.end
                        val offset = end - start
                        if (buffered == null)
                            buffered = inputStream.buffered(offset)
                        val buff = ByteArray(offset)
                        buffered?.read(buff)
                        sendByteString(buff.toByteString(0, offset))
                        Progress(start.toFloat() / end)
                    }
                    FILE_SEND_FINISHED -> {
                        Success
                    }
                    else -> {
                        Error(Exception())
                    }
                }
            }

        return startFlow
            .flatMapLatest {
                messagesFlow
            }
            .onEach {
                if (it is Error || it is Success) {
                    close()
                }
            }
    }

    fun sendByteString(message: ByteString) {
        println("send binary ${message.size}")
        webSocket.send(message)
    }

    fun <T> sendJson(message: T, clazz: ParameterizedType): WebSocketApi {
        val toJson = moshi.adapter<T>(clazz).toJson(message)
        println("send text $toJson")
        sendMessage(toJson)
        return this
    }

    fun <T> sendJson(message: T, clazz: Class<T>): WebSocketApi {
        val toJson = moshi.adapter(clazz).toJson(message)
        println("send text $toJson")
        sendMessage(toJson)
        return this
    }

    fun setOnConnectionChangedListener(listener: ConnectionListener) {
        this.connectionListener = listener
    }

    private fun sendMessage(toJson: String): Boolean {
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

    private fun Flow<SocketTransfer>.wrap(): Flow<SocketTransfer> =
        handleErrors().map {
            print("Transfer progress:")
            println(it)
            _progress.emit(it)
            it
        }
}
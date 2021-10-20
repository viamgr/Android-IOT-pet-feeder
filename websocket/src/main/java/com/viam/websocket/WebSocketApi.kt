package com.viam.websocket

import com.squareup.moshi.Moshi
import com.viam.websocket.model.*
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
    fun myLaunch(block: suspend () -> Unit): Job {
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
        myLaunch {
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
            .filterIsInstance<SocketEvent.Text>()
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
            .filterIsInstance<SocketEvent.Text>()
            .map {
                val socketMessage = moshi.adapter(SocketMessage::class.java).fromJson(it.data)
                return@map if (socketMessage?.key == key) {
                    return@map moshi.adapter(clazz).fromJson(it.data) as T
                } else null
            }
            .filterNotNull()
    }

    fun openWebSocket(request: Request) {
        /*   if (::webSocket.isInitialized) {
               return
           }*/
        webSocket = okHttpClient.newWebSocket(request, object : WebSocketListener() {
            override fun onOpen(webSocket: WebSocket, response: Response) {
                super.onOpen(webSocket, response)
                println("onOpen socket")
                isOpenedSocket = true
                myLaunch {
                    _events.emit(SocketEvent.Open)
                }
            }

            override fun onMessage(webSocket: WebSocket, text: String) {
                super.onMessage(webSocket, text)
                println("onMessage socket $text")
                myLaunch {
                    _events.emit(SocketEvent.Text(text))
                }
            }

            override fun onMessage(webSocket: WebSocket, bytes: ByteString) {
                super.onMessage(webSocket, bytes)
                println("onMessage socket $bytes")
                myLaunch {
                    _events.emit(SocketEvent.Binary(bytes))
                }
            }

            override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
                super.onClosing(webSocket, code, reason)
                isOpenedSocket = false
                println("onClosing socket")
                myLaunch {
                    _events.emit(SocketEvent.Closing)
                }
            }

            override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
                super.onClosed(webSocket, code, reason)
                isOpenedSocket = false
                println("onClosed socket")
                myLaunch {
                    _events.emit(SocketEvent.Closed)
                }
            }

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                super.onFailure(webSocket, t, response)
                isOpenedSocket = false
                println("onFailure socket")
                myLaunch {
                    _events.emit(SocketEvent.Failure(Exception(t)))
//                    delay(5000)
//                    openWebSocket()
                }
            }
        })
    }

    @Suppress("BlockingMethodInNonBlockingContext")
    fun receiveBinary(
        remoteFilePath: String,
        outputStream: OutputStream
    ): Flow<SocketTransfer> {
        cancelOldBinaryTransfer()
        println("Start Download: $remoteFilePath")

        fun clear() {
            binaryCoroutineContext = null
            outputStream.close()
        }
        return channelFlow {
            binaryCoroutineContext = currentCoroutineContext()
            fun onError(e: Throwable) {
                e.printStackTrace()
                throw e
            }

            var wrote = 0
            var size = 0

            CoroutineScope(currentCoroutineContext()).launch {
                sendJson(FileDetailRequest(remoteFilePath), FileDetailRequest::class.java)
                    .onMessageReceived(
                        FILE_DETAIL_CALLBACK,
                        FileDetailCallback::class.java
                    )
                    .collect { detailCallback: FileDetailCallback ->
                        size = detailCallback.size
                        send(SocketTransfer.Start(size, TransferType.Download))
                        sendJson(
                            FileRequestSlice(wrote),
                            FileRequestSlice::class.java
                        )
                    }
            }
            CoroutineScope(currentCoroutineContext()).launch {
                onBinaryReceived()
                    .collect {
                        val toByteArray = it.data.toByteArray()
                        outputStream.write(toByteArray)
                        wrote += toByteArray.size
                        if (wrote < size) {
                            sendJson(
                                FileRequestSlice(wrote),
                                FileRequestSlice::class.java
                            )
                            send(SocketTransfer.Progress(wrote.toFloat() / size))
                        } else {
                            sendJson(
                                SocketMessage(FILE_REQUEST_FINISHED),
                                SocketMessage::class.java
                            )
                            send(SocketTransfer.Progress(1F))
                            send(Success)

                            clear()
                        }
                    }
            }

            CoroutineScope(currentCoroutineContext()).launch {
                onMessageReceived<SocketMessage>(FILE_SEND_ERROR, SocketMessage::class.java)
                    .collect {
                        println("FILE_REQUEST_ERROR")
                        onError(Exception(FILE_REQUEST_ERROR))
                        clear()
                    }
            }

        }.onCompletion { e ->
            clear()
            println("onCompletion $e")
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    fun sendBinary(
        remoteFilePath: String,
        inputStream: InputStream,
    ): Flow<SocketTransfer> = channelFlow {
        cancelOldBinaryTransfer()
        var buffered: BufferedInputStream? = null

        fun close() {
            buffered?.close()
        }

        val startFlow: Flow<SocketTransfer> = flow {
            val size = inputStream.available()
            sendJson(SendFileMessage(remoteFilePath, size), SendFileMessage::class.java)
            emit(Start(size, TransferType.Upload))
        }

        val messagesFlow = events
            .filterIsInstance<SocketEvent.Text>().map {
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

        startFlow
            .flatMapLatest {
                messagesFlow
            }
            .onCompletion { e ->
                e?.let {
                    emit(Error(e as Exception))
                }
                close()
            }
            .collect {
                send(it)
                if (it is Success || it is Error) {
                    cancel()
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
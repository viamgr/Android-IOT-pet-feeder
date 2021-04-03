package com.viam.feeder.socket

import com.squareup.moshi.Moshi
import com.viam.feeder.socket.model.*
import kotlinx.coroutines.*
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.flow.*
import okhttp3.*
import okio.ByteString
import okio.ByteString.Companion.toByteString
import java.io.BufferedInputStream
import java.io.InputStream
import java.io.OutputStream
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton
import kotlin.coroutines.CoroutineContext

@Suppress("BlockingMethodInNonBlockingContext")
@Singleton
class WebSocketApi @Inject constructor(
    @Named("socket") val okHttpClient: OkHttpClient,
    val moshi: Moshi,
    private val request: Request
) {

    private val _events = MutableSharedFlow<SocketEvent>() // private mutable shared flow
    private val _progress =
        MutableSharedFlow<SocketTransfer>() // private mutable shared flow
    private lateinit var binaryCoroutineContext: CoroutineContext

    private val errorListeners = mutableListOf<(e: Exception) -> Unit>()
    val events = _events.asSharedFlow() // publicly exposed as read-only shared flow
    val progress = _progress.asSharedFlow() // publicly exposed as read-only shared flow

    private val socketRunnerScope = CoroutineScope(IO)
    lateinit var webSocket: WebSocket
    fun myLaunch(block: suspend () -> Unit): Job {
        return socketRunnerScope.launch {
            try {
                println("new Launch")
                block.invoke()
            } catch (e: Exception) {
                if (e !is CancellationException) {
                    e.printStackTrace()
                    onErrorHappened(e)
                }
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
    suspend fun <T : SocketMessage> onMessageReceived(
        key: String,
        clazz: (Class<in T>)? = SocketMessage::class.java
    ): Flow<T> = channelFlow {
        events
            .filterIsInstance<SocketEvent.Text>()
            .map {
                val socketMessage = moshi.adapter(SocketMessage::class.java).fromJson(it.data)
                return@map if (socketMessage?.key == key) {
                    return@map moshi.adapter(clazz).fromJson(it.data) as T
                } else null
            }
            .filterNotNull().collect {
                send(it)
            }
    }

    fun openWebSocket() {

        webSocket = okHttpClient.newWebSocket(request, object : WebSocketListener() {
            override fun onOpen(webSocket: WebSocket, response: Response) {
                super.onOpen(webSocket, response)
                myLaunch {
                    _events.emit(SocketEvent.Open)
                }
                println("onOpen socket")
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
                println("onClosing socket")
                myLaunch {
                    _events.emit(SocketEvent.Closing)
                }
            }

            override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
                super.onClosed(webSocket, code, reason)
                println("onClosed socket")
                myLaunch {
                    _events.emit(SocketEvent.Closed)
                }
            }

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                super.onFailure(webSocket, t, response)
                println("onFailure socket")
                myLaunch {
                    _events.emit(SocketEvent.Failure)
                    delay(5000)
                    openWebSocket()
                }
            }
        })

    }

    @Suppress("BlockingMethodInNonBlockingContext")
    fun receiveBinary(
        remoteFilePath: String,
        outputStream: OutputStream
    ): Flow<SocketTransfer> {
        fun clear() {
            outputStream.close()
        }
        return channelFlow {
            val job = this
            cancelOldBinaryTransfer()
            fun onError(e: Throwable) {
                e.printStackTrace()
                clear()
                job.cancel()
                throw e
            }

            var wrote = 0
            var size = 0

            CoroutineScope(currentCoroutineContext()).launch {
                sendJson(FileDetailRequest(remoteFilePath))
                    .onMessageReceived(FILE_DETAIL_CALLBACK, FileDetailCallback::class.java)
                    .collect { detailCallback: FileDetailCallback ->
                        size = detailCallback.size
                        send(SocketTransfer.Start(size, TransferType.Download))
                        sendJson(FileRequestSlice(wrote))
                    }
            }
            CoroutineScope(currentCoroutineContext()).launch {
                onBinaryReceived()
                    .collect {
                        val toByteArray = it.data.toByteArray()
                        outputStream.write(toByteArray)
                        wrote += toByteArray.size
                        if (wrote < size) {
                            sendJson(FileRequestSlice(wrote))
                            send(SocketTransfer.Progress(wrote.toFloat() / size))
                        } else {
                            sendJson(SocketMessage(FILE_REQUEST_FINISHED))
                            send(SocketTransfer.Progress(1F))
                            send(SocketTransfer.Success)
                        }
                    }
            }

            CoroutineScope(currentCoroutineContext()).launch {
                onMessageReceived<SocketMessage>(FILE_SEND_ERROR)
                    .collect {
                        println("FILE_REQUEST_ERROR")
                        onError(Exception(FILE_REQUEST_ERROR))
                        clear()
                    }
            }

        }.catch { e ->
            clear()
            throw e
        }.wrap()
    }

    @Suppress("BlockingMethodInNonBlockingContext")
    fun sendBinary(
        inputStream: InputStream,
        filePath: String
    ): Flow<SocketTransfer> {
        var buffered: BufferedInputStream? = null
        fun clear() {
            buffered?.close()
        }
        return channelFlow {
            cancelOldBinaryTransfer()
            val size = inputStream.available()
            send(SocketTransfer.Start(size, TransferType.Upload))

            suspend fun onError(e: Throwable) {
                e.printStackTrace()
                send(Result.failure<SocketTransfer>(e))
                clear()
                cancel()
                throw e
            }

            CoroutineScope(currentCoroutineContext()).launch {
                onMessageReceived(FILE_SEND_SLICE, ReceiveSliceMessage::class.java)
                    .collect { sliceMessage: ReceiveSliceMessage ->
                        val start = sliceMessage.start
                        val end = sliceMessage.end
                        val offset = end - start
                        if (buffered == null)
                            buffered = inputStream.buffered(offset)
                        val buff = ByteArray(offset)
                        buffered?.read(buff)
                        sendByteString(buff.toByteString(0, offset))
                        send(SocketTransfer.Progress(start.toFloat() / end))

                    }
            }
            CoroutineScope(currentCoroutineContext()).launch {
                onMessageReceived<SocketMessage>(FILE_SEND_ERROR)
                    .collect {
                        println(FILE_SEND_ERROR)
                        onError(Exception(FILE_SEND_ERROR))
                        clear()
                    }
            }
            CoroutineScope(currentCoroutineContext()).launch {
                onMessageReceived<SocketMessage>(FILE_SEND_FINISHED)
                    .collect {
                        println(FILE_SEND_FINISHED)
                        send(SocketTransfer.Progress(1F))
                        send(SocketTransfer.Success)
                        clear()
                    }
            }
            CoroutineScope(currentCoroutineContext()).launch {
                sendJson(SendFileMessage(filePath, size))
            }

        }.catch { e ->
            clear()
            throw e
        }.wrap()
    }

    fun sendByteString(message: ByteString) {
        println("send binary ${message.size}")
        webSocket.send(message)
    }

    inline fun <reified T : SocketMessage> sendJson(message: T): WebSocketApi {
        val toJson = moshi.adapter(T::class.java).toJson(message)
        println("send text $toJson")
        webSocket.send(toJson)
        return this
    }

    private suspend fun cancelOldBinaryTransfer() {
        if (::binaryCoroutineContext.isInitialized) {
            binaryCoroutineContext.cancel()
        }

        binaryCoroutineContext = currentCoroutineContext()
    }

    private fun Flow<SocketTransfer>.handleErrors(): Flow<SocketTransfer> = flow {
        try {
            collect { value ->
                emit(value)
            }
        } catch (e: Exception) {
            if (e !is CancellationException) {
                emit(SocketTransfer.Error(e))
            }
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
            _progress.emit(it)
            it
        }

}
package com.viam.feeder.data

import com.viam.websocket.model.FileDetailCallback
import com.viam.websocket.model.SocketEvent
import com.viam.websocket.model.SocketEvent.Text
import com.viam.websocket.model.SocketTransfer.Progress
import com.viam.websocket.model.SocketTransfer.Start
import com.viam.websocket.model.SocketTransfer.Success
import com.viam.websocket.model.TransferType.Download
import com.viam.websocket.sendEventWithCallbackCheck
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.produceIn
import kotlinx.coroutines.isActive
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.junit.Test

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
@OptIn(ExperimentalCoroutinesApi::class)

class WebSocketRepositoryTest {
    private val _events = MutableSharedFlow<SocketEvent>() // private mutable shared flow

    @Volatile var isDownloading = false

    @Test
    fun receive() = runBlocking {
        async {
            successEvent()
            async {
                val channel = _events.produceIn(CoroutineScope(currentCoroutineContext()))

                repeat(10) {
                    async {
                        download(channel)
                    }
                }

            }
        }
        delay(300000)

    }

    val mutex = Mutex()

    private suspend fun CoroutineScope.download(channel: ReceiveChannel<SocketEvent>) {
        mutex.withLock {
            return flow {
                val callback = requestGetDetailAndWaitForCallback(channel)
                val size = callback.size
                emit(Start(size, Download))
                var wrote = 0

                do {
                    val slice = sendSliceRequestAndWaitForCallback(channel, 1)
                    wrote++
                    emit(Progress(wrote.toFloat()))
                } while (isActive && wrote < size)

                emit(Success)
            }

                .onCompletion {
                    println("onCompletion $isDownloading")
                    isDownloading = false
                }
                .collect {
                    println(it)
                }

        }
        /*     if (isDownloading) {
                 return
             }
             isDownloading = true*/
    }

    private suspend fun requestGetDetailAndWaitForCallback(channel: ReceiveChannel<SocketEvent>): FileDetailCallback =
        coroutineScope {

            async {
                requestDetail()
            }
            val selectWithTimeout = channel.sendEventWithCallbackCheck(1000) {
                it is Text && it.data.startsWith("callback")
            } as Text
            // process selectWithTimeout
            println(selectWithTimeout)
            FileDetailCallback(1, 4)
        }

    private suspend fun requestDetail() {
        _events.emit(Text("callback"))
    }

    private suspend fun sendSliceRequestAndWaitForCallback(
        channel: ReceiveChannel<SocketEvent>,
        i: Int
    ): SocketEvent = coroutineScope {
        println("request $i")
        async {
            _events.emit(Text("binary${i + 1}"))
//            _events.emit(Text("error"))
        }
        channel
            .sendEventWithCallbackCheck(1000) {
                it.checkHasError()
                it.isBinary()
            }
    }

    private fun SocketEvent.isBinary() = this is Text && this.data.startsWith("binary")

    private suspend fun requestSlice(page: Int) {
        _events.emit(Text("binary$page"))
    }

    private fun finishReceive(finish: SocketEvent) =
        finish is Text && finish.data == "finish"

    private suspend fun CoroutineScope.successEvent() {
        async {
            println("produce")
            /*delay(200)
            _events.emit(Text("callback"))
            delay(200)
            _events.emit(Text("binary1"))
            delay(300)
            _events.emit(Text("binary2"))
            delay(300)
            _events.emit(Text("binary3"))
            delay(300)
            _events.emit(Text("binary4"))
            delay(500)
            _events.emit(Text("finish"))
            delay(1000)*/

        }
    }

    private suspend fun CoroutineScope.timeoutEvents() {
        async {
            println("produce")
            delay(300)
            _events.emit(Text("callback"))
            delay(300)
            _events.emit(Text("binary1"))
            delay(3000)
            _events.emit(Text("binary2"))
            delay(300)
            _events.emit(Text("binary3"))
            delay(300)
            _events.emit(Text("binary4"))
            delay(1000)
            _events.emit(Text("finish"))
            delay(1000)

        }
    }
}

private fun SocketEvent.checkHasError() {
    if (this is Text && this.data == "error") {
        throw Exception("Error in download")
    }
}

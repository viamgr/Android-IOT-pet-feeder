package com.viam.feeder.data

import com.squareup.moshi.Moshi
import com.viam.feeder.data.repository.WebSocketRepositoryImpl
import com.viam.websocket.WebSocketApi
import com.viam.websocket.model.SocketEvent
import com.viam.websocket.model.SocketEvent.Closed
import com.viam.websocket.model.SocketEvent.Open
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
@OptIn(ExperimentalCoroutinesApi::class)

class WebSocketRepositoryTest {
    private val _events = MutableSharedFlow<SocketEvent>() // private mutable shared flow

    lateinit var webSocketRepository: WebSocketRepositoryImpl

    @RelaxedMockK
    lateinit var moshi: Moshi

    @RelaxedMockK
    lateinit var webSocketApi: WebSocketApi

    @Before
    fun setup() {
        MockKAnnotations.init(this)
        every { webSocketApi.events } returns _events
        webSocketRepository = WebSocketRepositoryImpl(
            webSocketApi,
            moshi
        )
    }

    @Test
    fun aaaa() = runBlocking {

        async {
            async {
                delay(1000)
                _events.emit(Open)
                delay(1000)
                _events.emit(Closed)
                delay(1000)
                _events.emit(Open)
                delay(1000)

                _events.emit(Closed)
                delay(1000)
                _events.emit(Open)
                delay(1000)

                _events.emit(Closed)
                delay(1000)
                _events.emit(Open)
                delay(100000)
            }

            async {
                every { webSocketApi.isOpen() } returns true
                webSocketRepository.syncProcess(mockk(relaxed = true)).collect {
                    println(it)
                }
            }
        }

        /* async {
             async {
                 repeat(1000) {
                     _events.emit(it)
                     delay(500)
                 }
             }

             async {
                 _events
                     .filter { it % 5 == 0 }
                     .onCompletion {
                         println("onCompletion")
                     }
                     .collect {
                         println("it: $it")
                     }

             }
         }
 */
        delay(3000000)
    }
}

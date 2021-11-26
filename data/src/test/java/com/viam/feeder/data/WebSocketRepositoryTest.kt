package com.viam.feeder.data

import com.squareup.moshi.Moshi
import com.viam.feeder.data.repository.WebSocketRepositoryImpl
import com.viam.websocket.WebSocketApi
import com.viam.websocket.model.SocketEvent
import com.viam.websocket.model.SocketEvent.Closed
import com.viam.websocket.model.SocketEvent.Open
import com.viam.websocket.waitForCallback
import io.mockk.MockKAnnotations
import io.mockk.impl.annotations.RelaxedMockK
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.produceIn
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
    }

    @Test
    fun aaaa() = runBlocking {

        async {
            async {
                delay(2000)
                repeat(2000) {
                    _events.emit(Open)
                }
                _events.emit(Closed)

                delay(100000)
            }

            async {

                var i = 0
                do {
                    val a = _events.produceIn(this).waitForCallback(takeWhile = {
                        it is Open
                    })
                    if (a is Closed) {
                        break
                    }
                    i++
                    println("result2: $i")
                } while (true)

            }
            delay(550000)
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

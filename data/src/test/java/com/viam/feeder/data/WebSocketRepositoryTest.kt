package com.viam.feeder.data

import com.squareup.moshi.Moshi
import com.viam.feeder.data.repository.WebSocketRepositoryImpl
import com.viam.websocket.WebSocketApi
import com.viam.websocket.model.SocketEvent
import com.viam.websocket.model.SocketEvent.Closed
import com.viam.websocket.waitForCallbacka
import io.mockk.MockKAnnotations
import io.mockk.impl.annotations.RelaxedMockK
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
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
    private val _events = MutableStateFlow<SocketEvent?>(null) // private mutable shared flow

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
//                delay(2000)
                repeat(200) {
                    println("emitting $it")
                    _events.emit(SocketEvent.Text(it.toString()))
                    delay(3000)
                }
                _events.emit(Closed)

                delay(100000)
            }

            async {

                val a = _events.waitForCallbacka(takeWhile = {
                    println("checking $it")
                    it is SocketEvent.Text
                })

                println("a $a")
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

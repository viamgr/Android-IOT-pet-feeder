package com.viam.feeder.data

import com.squareup.moshi.Moshi
import com.viam.feeder.data.repository.WebSocketRepositoryImpl
import com.viam.feeder.domain.repositories.system.JsonPreferences
import com.viam.websocket.WebSocketApi
import com.viam.websocket.model.SocketEvent
import com.viam.websocket.model.SocketEvent.Closed
import com.viam.websocket.model.SocketEvent.Open
import com.viam.websocket.model.SocketEvent.Text
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.RelaxedMockK
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import java.io.File
import javax.inject.Named

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {

    private val _events = MutableSharedFlow<SocketEvent>() // private mutable shared flow

    @RelaxedMockK
    lateinit var webSocketApi: WebSocketApi

    @RelaxedMockK
    lateinit var moshi: Moshi

    @RelaxedMockK
    lateinit var jsonPreferences: JsonPreferences

    @RelaxedMockK
    @Named("configFile")
    lateinit var configFile: File

    lateinit var webSocketRepositoryImpl: WebSocketRepositoryImpl

    @Before
    fun setup() {
        MockKAnnotations.init(this)
        every { webSocketApi.events } returns _events
        webSocketRepositoryImpl = WebSocketRepositoryImpl(
            webSocketApi,
            jsonPreferences,
            moshi
        )
    }

    @Test
    fun subscribe() = runBlocking {
        async {

            runEvents()
            async {
                val resource = this@ExampleUnitTest.javaClass.classLoader.getResourceAsStream("test.json")
                /* val fileOutputStream = File(resource)
                 webSocketRepositoryImpl.subscribeAndPair(fileOutputStream.strea).collect {
                     println("finally $it")
                 }*/
            }

        }

        delay(30000)
    }

    private suspend fun CoroutineScope.runEvents() {
        async {
            println("produce")
            delay(300)
            _events.emit(Open)
            delay(300)
            _events.emit(Open)
            delay(300)
            _events.emit(Open)
            delay(300)
            _events.emit(Open)
            delay(300)
            _events.emit(Text("time"))
            delay(1000)
            _events.emit(Text("subscribe:reject"))
            delay(1000)
            _events.emit(Text("pair:done"))
            delay(1000)
            _events.emit(Text("Text ${System.currentTimeMillis()}"))

            _events.emit(Closed)

            delay(10000)
            _events.emit(Open)
            delay(300)
            _events.emit(Open)
            delay(300)
            _events.emit(Open)
            delay(300)
            _events.emit(Open)
            delay(300)
            _events.emit(Open)
            delay(300)
            _events.emit(Text("time"))
            delay(1000)
            _events.emit(Text("subscribe:done"))
            delay(1000)
            _events.emit(Text("pair:done"))
            delay(1000)
            _events.emit(Text("Text ${System.currentTimeMillis()}"))
            _events.emit(Closed)
            delay(1000)

            _events.emit(Open)
            delay(300)
            _events.emit(Text("time"))
            delay(1000)
            _events.emit(Text("subscribe:done"))
            delay(1000)
            _events.emit(Text("pair:done"))
            delay(1000)
            _events.emit(Text("Text ${System.currentTimeMillis()}"))

            delay(1000000)

            _events.emit(Closed)
        }
    }

    @Test
    fun addition_isCorrect() {
        assertEquals(4, 2 + 2)
    }
}
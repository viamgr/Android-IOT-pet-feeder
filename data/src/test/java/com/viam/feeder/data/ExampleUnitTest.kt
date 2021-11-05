package com.viam.feeder.data

import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {
/*
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
                *//* val fileOutputStream = File(resource)
                 webSocketRepositoryImpl.subscribeAndPair(fileOutputStream.strea).collect {
                     println("finally $it")
                 }*//*
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
    }*/

    @Test
    fun addition_isCorrect() {
        assertEquals(4, 2 + 2)
    }
}
package com.viam.websocket

import com.viam.websocket.model.SocketEvent
import com.viam.websocket.model.SocketEvent.Text
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.selects.select
import java.util.concurrent.TimeoutException

@OptIn(ExperimentalCoroutinesApi::class)
suspend fun StateFlow<SocketEvent>.waitForCallbacka(
    successKey: String,
    errorKey: String,
    timeout: Long? = 15000
): SocketEvent {
    println("waitForCallback $successKey")
    return waitForCallbacka(timeout = timeout, takeWhile = {
        println("new event check on $successKey with $it")
        it.checkHasError(errorKey)
        println("result of check is ${it.containsKey(successKey)}")
        it.containsKey(successKey)
    })
}

fun SocketEvent.containsKey(key: String): Boolean {
    val regex = "\"key\":[\\s\\S]*\"$key\"".toRegex()
    return this is Text && (this.data.contains(regex))
}

fun SocketEvent.checkHasError(errorKey: String) {
    if (this is Text && this.containsKey(errorKey)) {
        throw Exception(this.data)
    }
}

@OptIn(ExperimentalCoroutinesApi::class)
suspend fun ReceiveChannel<SocketEvent>.waitForCallback(
    takeWhile: (SocketEvent) -> Boolean,
    timeout: Int? = null
): SocketEvent {
    val timeoutValue = timeout ?: 5000
    val endTime = System.currentTimeMillis() + timeoutValue

    do {
        val event = select<SocketEvent> {
            val timeMillis = endTime - System.currentTimeMillis()
            onTimeout(timeMillis) {
                println("failed to get after $timeMillis millis")
                throw TimeoutException("after $timeoutValue")
            }
            onReceive { value ->  // this is the first select clause
                println("this is the first select clause $value")
                value
            }
        }
        if (takeWhile(event)) {
            return event
        } else {
            println("continue getting after $event")
        }
    } while (true)
}

@OptIn(ExperimentalCoroutinesApi::class)
suspend fun StateFlow<SocketEvent?>.waitForCallbacka(
    takeWhile: (SocketEvent) -> Boolean,
    timeout: Long? = null
): SocketEvent = coroutineScope {
    val currentTime = System.currentTimeMillis()

    var lastCheckedEvent = value.hashCode()
    do {
        if (lastCheckedEvent.hashCode() != value.hashCode()) {

            val take = takeWhile(value!!)
            if (take) {
                println("waitForCallback result $value")
                return@coroutineScope value!!
            }
            lastCheckedEvent = value.hashCode()
        }
    } while (System.currentTimeMillis() - currentTime < (timeout ?: 10000))

    throw TimeoutException()
}

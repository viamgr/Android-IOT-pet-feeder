package com.viam.websocket

import com.viam.websocket.model.SocketEvent
import com.viam.websocket.model.SocketEvent.Text
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.selects.select
import java.util.concurrent.TimeoutException

@OptIn(ExperimentalCoroutinesApi::class)
suspend fun ReceiveChannel<SocketEvent>.waitForCallback(
    successKey: String,
    errorKey: String,
    timeout: Int? = 5000
): SocketEvent {

    val sendEventWithCallbackCheck = try {
        waitForCallback(timeout = timeout, takeWhile = {
            println("new event check on $successKey with $it")
            it.checkHasError(errorKey)
            it.containsKey(successKey)
        })
    } catch (e: TimeoutException) {
        throw TimeoutException(successKey)
    } catch (e: Exception) {
        throw e
    }
    return sendEventWithCallbackCheck
}

fun SocketEvent.containsKey(key: String) = this is Text && (this.data.contains("\"key\":\"$key\""))

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
    val endTime = System.currentTimeMillis() + (timeout ?: 5000)
    do {
        val event = select<SocketEvent> {
            val timeMillis = endTime - System.currentTimeMillis()
            onTimeout(timeMillis) {
                println("failed to get after $timeMillis millis")
                throw TimeoutException("after $timeMillis")
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

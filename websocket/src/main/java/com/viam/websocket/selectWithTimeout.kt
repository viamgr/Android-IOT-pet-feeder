package com.viam.websocket

import com.viam.websocket.model.SocketEvent
import com.viam.websocket.model.SocketEvent.Text
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.selects.select
import java.util.concurrent.TimeoutException

@OptIn(ExperimentalCoroutinesApi::class)
suspend fun ReceiveChannel<SocketEvent>.sendEventWithCallbackCheck(
    successKey: String,
    errorKey: String,
    timeout: Int? = 5000
): SocketEvent {

    return sendEventWithCallbackCheck(timeout) {
        it.checkHasError(errorKey)
        it.containsKey(successKey)
    }
}

private fun SocketEvent.containsKey(key: String) = this is Text && (this.data.contains("\"key\":\"$key\""))

fun SocketEvent.checkHasError(errorKey: String) {
    if (this is Text && this.containsKey(errorKey)) {
        throw Exception(this.data)
    }
}

@OptIn(ExperimentalCoroutinesApi::class)
suspend fun ReceiveChannel<SocketEvent>.sendEventWithCallbackCheck(
    timeout: Int?,
    takeWhile: (SocketEvent) -> Boolean
): SocketEvent {
    val endTime = System.currentTimeMillis() + (timeout ?: 5000)
    do {
        val event = select<SocketEvent> {
            onTimeout(endTime - System.currentTimeMillis()) { throw TimeoutException() }
            onReceive { value ->  // this is the first select clause
                value
            }
        }
        if (takeWhile(event)) {
            return event
        }
    } while (true)
}

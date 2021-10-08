package com.viam.websocket.model

import okio.ByteString

sealed class SocketEvent {
    data class Text(val data: String) : SocketEvent()
    data class Binary(val data: ByteString) : SocketEvent()
    object Open : SocketEvent()
    object Closing : SocketEvent()
    object Closed : SocketEvent()
    data class Failure(val exception: Exception) : SocketEvent()
}

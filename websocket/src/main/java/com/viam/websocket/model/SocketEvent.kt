package com.viam.websocket.model

sealed class SocketEvent {
    data class Text(val data: String) : SocketEvent()
    data class Binary(val data: ByteArray) : SocketEvent() {
        override fun equals(other: Any?): Boolean {
            /*if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as Binary

            if (!data.contentEquals(other.data)) return false
*/
            return data.hashCode() == other.hashCode()
        }

        override fun hashCode(): Int {
            return data.hashCode()
        }
    }

    object Init : SocketEvent()
    object Open : SocketEvent()
    object Closing : SocketEvent()
    object Closed : SocketEvent()
    data class Failure(val exception: Exception) : SocketEvent()
}

package com.viam.feeder.socket.model

sealed class SocketTransfer {
    data class Start(val size: Int) : SocketTransfer()
    data class Error(val exception: Throwable) : SocketTransfer()
    data class Progress(val progress: Float) : SocketTransfer()
    object Success : SocketTransfer()
}
package com.viam.feeder.socket.model

sealed class SocketTransfer {
    data class Start(val size: Int, val transferType: TransferType) : SocketTransfer()
    data class Error(val exception: Throwable) : SocketTransfer()
    data class Progress(val progress: Float) : SocketTransfer()
    object Success : SocketTransfer()
}

enum class TransferType {
    Upload, Download
}
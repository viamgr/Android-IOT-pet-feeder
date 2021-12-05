package com.viam.websocket.model

sealed class SocketTransfer {
    data class Start(val size: Int, val transferType: TransferType) : SocketTransfer()
    data class Error(val exception: Exception) : SocketTransfer()
    data class Progress(val progress: Float) : SocketTransfer()
    object Success : SocketTransfer()
}

sealed class SocketConnectionStatus {
    data class Failure(val exception: Exception) : SocketConnectionStatus()
    object Subscribing : SocketConnectionStatus()
    object Subscribed : SocketConnectionStatus()
    object Pairing : SocketConnectionStatus()
    data class Paired(val deviceName: String) : SocketConnectionStatus()
    data class Configuring(val progress: Float) : SocketConnectionStatus()
    object Configured : SocketConnectionStatus()
}

enum class TransferType {
    Upload, Download
}
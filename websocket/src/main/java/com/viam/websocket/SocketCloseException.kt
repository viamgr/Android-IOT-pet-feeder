package com.viam.websocket

class SocketCloseException(val text: String = "Socket is Closed", cause: Throwable? = null) :
    Exception(text, cause)
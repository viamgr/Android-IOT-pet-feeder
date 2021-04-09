package com.viam.websocket

class FailedToSendException :
    Exception("Failed to send web socket message. Ary you sure is connected to the server?")
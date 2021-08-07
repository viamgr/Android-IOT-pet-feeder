package com.viam.feeder.model

import com.squareup.moshi.JsonClass
import com.viam.websocket.model.SocketMessage

@JsonClass(generateAdapter = true)
data class KeyValueMessage<T>(override var key: String, val value: T) :
    SocketMessage(key)
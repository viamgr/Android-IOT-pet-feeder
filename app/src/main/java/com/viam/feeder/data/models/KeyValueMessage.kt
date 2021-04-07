package com.viam.feeder.data.models

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class KeyValueMessage<T>(override var key: String, val value: T) :
    com.viam.websocket.model.SocketMessage(key)
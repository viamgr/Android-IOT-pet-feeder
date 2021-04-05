package com.viam.feeder.data.models

import com.squareup.moshi.JsonClass
import com.viam.feeder.socket.model.SocketMessage

@JsonClass(generateAdapter = true)
data class KeyValueMessage<T>(override var key: String, val value: T) : SocketMessage(key)
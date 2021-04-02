package com.viam.feeder.socket.model

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
open class SocketMessage(var key: String)
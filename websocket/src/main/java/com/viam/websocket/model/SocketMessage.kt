package com.viam.websocket.model

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
open class SocketMessage(open var key: String)
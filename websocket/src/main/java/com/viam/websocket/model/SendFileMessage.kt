package com.viam.websocket.model

import com.squareup.moshi.JsonClass
import com.viam.websocket.FILE_SEND_START

@JsonClass(generateAdapter = true)
data class SendFileMessage(val name: String, val size: Int) : SocketMessage(FILE_SEND_START)

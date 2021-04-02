package com.viam.feeder.socket.model

import com.squareup.moshi.JsonClass
import com.viam.feeder.socket.FILE_SEND_START

@JsonClass(generateAdapter = true)
data class SendFileMessage(val name: String, val size: Int) : SocketMessage(FILE_SEND_START)

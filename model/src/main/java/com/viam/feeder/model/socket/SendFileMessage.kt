package com.viam.feeder.model.socket

import com.squareup.moshi.JsonClass
import com.viam.feeder.shared.FILE_SEND_START
import com.viam.websocket.model.SocketMessage

@JsonClass(generateAdapter = true)
data class SendFileMessage(val name: String, val size: Int) : SocketMessage(FILE_SEND_START)

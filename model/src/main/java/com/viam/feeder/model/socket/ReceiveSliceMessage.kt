package com.viam.feeder.model.socket

import com.squareup.moshi.JsonClass
import com.viam.feeder.shared.FILE_SEND_SLICE
import com.viam.websocket.model.SocketMessage

@JsonClass(generateAdapter = true)
data class ReceiveSliceMessage(val start: Int, val end: Int) : SocketMessage(FILE_SEND_SLICE)

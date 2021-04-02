package com.viam.feeder.socket.model

import com.squareup.moshi.JsonClass
import com.viam.feeder.socket.FILE_SEND_SLICE

@JsonClass(generateAdapter = true)
data class ReceiveSliceMessage(val start: Int, val end: Int) : SocketMessage(FILE_SEND_SLICE)

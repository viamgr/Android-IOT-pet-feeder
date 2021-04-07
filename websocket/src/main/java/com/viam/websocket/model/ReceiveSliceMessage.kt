package com.viam.websocket.model

import com.squareup.moshi.JsonClass
import com.viam.websocket.FILE_SEND_SLICE

@JsonClass(generateAdapter = true)
data class ReceiveSliceMessage(val start: Int, val end: Int) : SocketMessage(FILE_SEND_SLICE)

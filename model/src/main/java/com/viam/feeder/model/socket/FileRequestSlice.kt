package com.viam.feeder.model.socket

import com.squareup.moshi.JsonClass
import com.viam.feeder.shared.FILE_REQUEST_SLICE
import com.viam.websocket.model.SocketMessage

@JsonClass(generateAdapter = true)
data class FileRequestSlice(val start: Int) : SocketMessage(FILE_REQUEST_SLICE)

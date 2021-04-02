package com.viam.feeder.socket.model

import com.squareup.moshi.JsonClass
import com.viam.feeder.socket.FILE_REQUEST_SLICE

@JsonClass(generateAdapter = true)
data class FileRequestSlice(val start: Int) : SocketMessage(FILE_REQUEST_SLICE)

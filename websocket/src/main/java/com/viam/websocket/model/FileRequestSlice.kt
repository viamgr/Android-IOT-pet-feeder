package com.viam.websocket.model

import com.squareup.moshi.JsonClass
import com.viam.websocket.FILE_REQUEST_SLICE

@JsonClass(generateAdapter = true)
data class FileRequestSlice(val start: Int) : SocketMessage(FILE_REQUEST_SLICE)

package com.viam.websocket.model

import com.squareup.moshi.JsonClass
import com.viam.websocket.FILE_DETAIL_CALLBACK

@JsonClass(generateAdapter = true)
data class FileDetailCallback(val buffer: Int, val size: Int) : SocketMessage(FILE_DETAIL_CALLBACK)

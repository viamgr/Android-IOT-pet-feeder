package com.viam.feeder.socket.model

import com.squareup.moshi.JsonClass
import com.viam.feeder.socket.FILE_DETAIL_CALLBACK

@JsonClass(generateAdapter = true)
data class FileDetailCallback(val buffer: Int, val size: Int) : SocketMessage(FILE_DETAIL_CALLBACK)

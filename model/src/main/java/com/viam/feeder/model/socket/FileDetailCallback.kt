package com.viam.feeder.model.socket

import com.squareup.moshi.JsonClass
import com.viam.feeder.shared.FILE_DETAIL_CALLBACK
import com.viam.websocket.model.SocketMessage

@JsonClass(generateAdapter = true)
data class FileDetailCallback(val buffer: Int, val size: Int) : SocketMessage(FILE_DETAIL_CALLBACK)

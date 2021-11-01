package com.viam.feeder.model.socket

import com.squareup.moshi.JsonClass
import com.viam.feeder.shared.FILE_DETAIL_REQUEST
import com.viam.websocket.model.SocketMessage

@JsonClass(generateAdapter = true)
data class FileDetailRequest(val name: String) : SocketMessage(FILE_DETAIL_REQUEST)

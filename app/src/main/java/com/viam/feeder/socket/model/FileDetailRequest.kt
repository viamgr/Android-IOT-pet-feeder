package com.viam.feeder.socket.model

import com.squareup.moshi.JsonClass
import com.viam.feeder.socket.FILE_DETAIL_REQUEST

@JsonClass(generateAdapter = true)
data class FileDetailRequest(val name: String) : SocketMessage(FILE_DETAIL_REQUEST)

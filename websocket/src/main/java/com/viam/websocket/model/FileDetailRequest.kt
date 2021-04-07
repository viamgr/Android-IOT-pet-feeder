package com.viam.websocket.model

import com.squareup.moshi.JsonClass
import com.viam.websocket.FILE_DETAIL_REQUEST

@JsonClass(generateAdapter = true)
data class FileDetailRequest(val name: String) : SocketMessage(FILE_DETAIL_REQUEST)

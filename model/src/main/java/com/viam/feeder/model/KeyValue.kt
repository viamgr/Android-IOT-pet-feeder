package com.viam.feeder.model

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class KeyValue(var key: String, var value: Any? = null)

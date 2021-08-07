package com.viam.feeder.model

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class WifiDevice(val bssid: String, val ssid: String, val secure: Int)
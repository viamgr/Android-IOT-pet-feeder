package com.viam.feeder.data.models

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class WifiDevice(val bssid: String, val ssid: String, val secure: Int)
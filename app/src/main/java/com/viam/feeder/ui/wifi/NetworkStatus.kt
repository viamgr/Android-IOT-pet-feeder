package com.viam.feeder.ui.wifi

data class NetworkStatus(
    val deviceName: String?,
    val isAvailable: Boolean,
    val isWifi: Boolean,
)
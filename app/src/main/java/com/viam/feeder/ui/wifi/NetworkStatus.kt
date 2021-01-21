package com.viam.feeder.ui.wifi

data class NetworkStatus(
    val deviceName: String?,
    val isAvailable: Boolean,
    val isWifi: Boolean,
) {
    fun isConnectedToPreferredDevice(ssid: String): Boolean {
        return isAvailable && isWifi && deviceName == "\"$ssid\""
    }

    fun isUnknownWifi(): Boolean {
        return isAvailable && isWifi && (deviceName == null)
    }

    fun isEnoughWifiConnection(ssid: String): Boolean {
        return isAvailable && isWifi && (deviceName == null || deviceName == "\"$ssid\"")
    }
}
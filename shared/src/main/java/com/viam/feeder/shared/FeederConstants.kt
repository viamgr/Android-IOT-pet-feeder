package com.viam.feeder.shared

class FeederConstants {

    companion object {
        const val CONFIG_FILE_PATH = "config.json"
    }

    enum class WifiMode {
        WIFI_MODE_OFF,
        WIFI_MODE_AP,
        WIFI_MODE_STA,
        WIFI_MODE_AP_STA
    }
}
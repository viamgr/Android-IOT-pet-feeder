package com.viam.feeder.constants

import androidx.annotation.StringDef


const val SETTING_FEEDING_INTERVAL = "SETTING_FEEDING_INTERVAL"
const val SETTING_SOUND_VOLUME = "SETTING_SOUND_VOLUME"
const val SETTING_FEED_VOLUME = "FEED_VOLUME"
const val SETTING_LED_STATE = "LED_STATE"
const val SETTING_WIFI_PASS = "WIFI_PASS"

@Retention(AnnotationRetention.SOURCE)
@StringDef(
    SETTING_FEEDING_INTERVAL,
    SETTING_SOUND_VOLUME,
    SETTING_FEED_VOLUME,
    SETTING_LED_STATE,
    SETTING_WIFI_PASS,
)
annotation class Settings
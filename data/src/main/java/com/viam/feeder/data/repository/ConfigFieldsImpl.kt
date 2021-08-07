package com.viam.feeder.data.repository

import androidx.lifecycle.LiveData
import com.squareup.moshi.Moshi
import com.viam.feeder.domain.repositories.system.ConfigFields
import com.viam.feeder.domain.repositories.system.JsonPreferences
import javax.inject.Inject

class ConfigFieldsImpl @Inject constructor(
    configObject: JsonPreferences,
    moshi: Moshi
) : ConfigFields {
    private var soundVolume = LiveDataConfig(configObject, "soundVolume", 3.99F)
    private var wifiSsid = LiveDataConfig(configObject, "wifiSsid", "")
    private var wifiPassword = LiveDataConfig(configObject, "wifiPassword", "")
    private var feedingDuration = LiveDataConfig(configObject, "feedingDuration", 2000)
    private var ledState = LiveDataConfig(configObject, "ledState", 2)
    private var ledTurnOffDelay = LiveDataConfig(configObject, "ledTurnOffDelay", 60000)
    private var alarms = ArrayConfig<String>(
        moshi = moshi,
        configObject = configObject,
        name = "alarms",
        defaultValue = emptyList(),
        type = String::class.java
    )

    override fun getAlarms(): LiveData<List<String>> {
        return alarms
    }

    override fun getSoundVolume(): LiveData<Float> {
        return soundVolume
    }

    override fun getWifiSsid(): LiveData<String> {
        return wifiSsid
    }

    override fun getWifiPassword(): LiveData<String> {
        return wifiPassword
    }

    override fun getFeedingDuration(): LiveData<Int> {
        return feedingDuration
    }

    override fun getLedState(): LiveData<Int> {
        return ledState
    }

    override fun getLedTurnOffDelay(): LiveData<Int> {
        return ledTurnOffDelay
    }

    override fun setAlarms(list: List<String>) {
        return alarms.store(list)
    }

    override fun setFeedingDuration(value: Int) {
        feedingDuration.store(value)
    }

    override fun setLedTurnOffDelay(value: Int) {
        ledTurnOffDelay.store(value)
    }

    override fun setSoundVolume(value: Float) {
        soundVolume.store(value)
    }

    override fun setWifiSsid(value: String) {
        wifiSsid.store(value)
    }

    override fun setWifiPassword(value: String) {
        wifiPassword.store(value)
    }
}
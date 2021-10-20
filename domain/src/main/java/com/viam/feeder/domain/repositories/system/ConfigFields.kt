package com.viam.feeder.domain.repositories.system

import androidx.lifecycle.LiveData
import com.viam.feeder.shared.FeederConstants.WifiMode

interface ConfigFields {
    fun getAlarms(): LiveData<List<String>>
    fun getSoundVolume(): LiveData<Float>
    fun getWifiSsid(): LiveData<String>
    fun getWifiPassword(): LiveData<String>
    fun getFeedingDuration(): LiveData<Int>
    fun getLedState(): LiveData<Int>
    fun getLedTurnOffDelay(): LiveData<Int>
    fun setAlarms(list: List<String>)
    fun setFeedingDuration(value: Int)
    fun setLedTurnOffDelay(value: Int)
    fun setSoundVolume(value: Float)
    fun setWifiSsid(value: String)
    fun setWifiPassword(value: String)
    fun setWifiMode(value: WifiMode)
    fun setStaticIp(value: String)
    fun setUseDhcp(value: Int)
    fun setSubnet(value: String)
    fun setGateway(value: String)
}
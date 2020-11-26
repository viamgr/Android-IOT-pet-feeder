package com.viam.feeder.ui.setting

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.ViewModel
import com.viam.feeder.core.domain.toLiveTask
import com.viam.feeder.core.task.compositeTask
import com.viam.feeder.data.domain.wifi.ConnectWifi
import com.viam.feeder.data.domain.wifi.GetWifiList
import com.viam.feeder.data.domain.wifi.WifiAuthentication
import com.viam.feeder.data.models.WifiDevice

class SettingViewModel @ViewModelInject constructor(
    getWifiList: GetWifiList,
    connectWifi: ConnectWifi
) : ViewModel() {

    val getWifiListTask = getWifiList.toLiveTask().also {
        it.post(Unit)
    }

    private val connectWifiTask = connectWifi.toLiveTask()

    val compositeTask = compositeTask(
        getWifiListTask,
        connectWifiTask
    )

    fun onPasswordConfirmed(wifiDevice: WifiDevice, password: String) {
        connectWifiTask.post(WifiAuthentication(wifiDevice.ssid, password))
    }
}
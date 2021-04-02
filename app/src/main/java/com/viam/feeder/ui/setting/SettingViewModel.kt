package com.viam.feeder.ui.setting

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.ViewModel
import com.viam.feeder.core.task.livaTask
import com.viam.feeder.core.utility.launchInScope
import com.viam.feeder.data.domain.config.SetWifiCredentials
import com.viam.feeder.data.domain.config.WifiAuthentication
import com.viam.feeder.data.domain.wifi.GetWifiList
import com.viam.feeder.data.models.WifiDevice
import kotlinx.coroutines.delay

class SettingViewModel @ViewModelInject constructor(
    getWifiList: GetWifiList,
    private val setWifiCredentials: SetWifiCredentials
) : ViewModel() {
    val getWifiListTask = livaTask<Unit, List<WifiDevice>> {
        emit(getWifiList(Unit))
        delay(1000)
        repeat(500) {
            emit(getWifiList(Unit))
            delay(15000)
        }
    }.execute(Unit)

    fun onPasswordConfirmed(wifiDevice: WifiDevice, password: String) = launchInScope {
        setWifiCredentials(WifiAuthentication(wifiDevice.ssid, password))
    }
}
package com.viam.feeder.ui.setting

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.ViewModel
import com.viam.feeder.constants.WIFI_LIST_IS
import com.viam.feeder.constants.WIFI_LIST_WITCH
import com.viam.feeder.core.utility.launchInScope
import com.viam.feeder.data.domain.config.SetWifiCredentials
import com.viam.feeder.data.domain.config.WifiAuthentication
import com.viam.feeder.data.domain.event.SendEvent
import com.viam.feeder.data.domain.wifi.WifiList
import com.viam.feeder.data.models.WifiDevice
import kotlinx.coroutines.delay

class SettingViewModel @ViewModelInject constructor(
    wifiList: WifiList,
    sendEvent: SendEvent,
    private val setWifiCredentials: SetWifiCredentials
) : ViewModel() {

    val getWifiListTask =
        wifiList(WIFI_LIST_IS).also {
            launchInScope {
                sendEvent(WIFI_LIST_WITCH)
                delay(1000)
                repeat(500) {
                    sendEvent(WIFI_LIST_WITCH)
                    delay(10000)
                }
            }
        }

    fun onPasswordConfirmed(wifiDevice: WifiDevice, password: String) = launchInScope {
        setWifiCredentials(WifiAuthentication(wifiDevice.ssid, password))
    }
}
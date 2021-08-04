package com.viam.feeder.ui.setting

import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import com.part.livetaskcore.usecases.asLiveTask
import com.viam.feeder.constants.WIFI_LIST_WITCH
import com.viam.feeder.core.utility.launchInScope
import com.viam.feeder.data.domain.config.SetWifiCredentials
import com.viam.feeder.data.domain.config.WifiAuthentication
import com.viam.feeder.data.domain.event.SendEvent
import com.viam.feeder.data.domain.wifi.GetWifiList
import com.viam.feeder.data.models.WifiDevice
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import javax.inject.Inject

@HiltViewModel
class SettingViewModel @Inject constructor(
    wifiList: GetWifiList,
    sendEvent: SendEvent,
    private val setWifiCredentials: SetWifiCredentials
) : ViewModel() {
    val sendEventTask = sendEvent.asLiveTask()
    val getWifiListTask = wifiList(Unit).asLiveData()

    init {
        requestToGetWifiList()
    }

    private fun requestToGetWifiList() = launchInScope {
        sendEventTask(WIFI_LIST_WITCH)
        delay(1000)
        while (currentCoroutineContext().isActive) {
            sendEventTask(WIFI_LIST_WITCH)
            delay(5000)
        }
    }

    fun onPasswordConfirmed(wifiDevice: WifiDevice, password: String) = launchInScope {
        setWifiCredentials(WifiAuthentication(wifiDevice.ssid, password))
    }
}
package com.viam.feeder.ui.setting

import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import com.part.livetaskcore.usecases.asLiveTask
import com.viam.feeder.core.utility.launchInScope
import com.viam.feeder.data.constants.WIFI_LIST_WITCH
import com.viam.feeder.data.datasource.RemoteConnectionConfig
import com.viam.feeder.domain.usecase.config.SetWifiCredentials
import com.viam.feeder.domain.usecase.config.WifiAuthentication
import com.viam.feeder.domain.usecase.device.AddDevice
import com.viam.feeder.domain.usecase.event.SendEvent
import com.viam.feeder.domain.usecase.wifi.GetWifiList
import com.viam.feeder.model.Device
import com.viam.feeder.model.WifiDevice
import com.viam.feeder.shared.DEFAULT_ACCESS_POINT_IP
import com.viam.feeder.shared.DEFAULT_ACCESS_POINT_PORT
import com.viam.resource.Resource
import com.viam.websocket.model.SocketTransfer
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import javax.inject.Inject

@HiltViewModel
class SettingViewModel @Inject constructor(
    wifiList: GetWifiList,
    sendEvent: SendEvent,
    addDevice: AddDevice,
    setWifiCredentials: SetWifiCredentials,
    private val remoteConnectionConfig: RemoteConnectionConfig
) : ViewModel() {
    val sendEventTask = sendEvent.asLiveTask()
    val addDeviceTask = addDevice.asLiveTask()
    val getWifiListTask = wifiList(Unit).asLiveData()
    val setWifiCredentialsTask = setWifiCredentials.asLiveTask {
        onSuccess<Resource<SocketTransfer>> {
            val parameter = getParameter()
            addDevice(parameter)
        }
    }

    init {
        requestToGetWifiList()
    }

    private fun addDevice(parameter: WifiAuthentication) = launchInScope {
        addDeviceTask(Device(1, "Device1", parameter.ip, parameter.port, parameter.gateway))
        remoteConnectionConfig.url = parameter.ip ?: DEFAULT_ACCESS_POINT_IP
        remoteConnectionConfig.port = parameter.port ?: DEFAULT_ACCESS_POINT_PORT
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
        setWifiCredentialsTask(WifiAuthentication(wifiDevice.ssid, password))
    }
}
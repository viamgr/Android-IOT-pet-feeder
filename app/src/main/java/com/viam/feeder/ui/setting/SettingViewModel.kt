package com.viam.feeder.ui.setting

import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import com.part.livetaskcore.livatask.combine
import com.part.livetaskcore.usecases.asLiveTask
import com.viam.feeder.core.utility.launchInScope
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
import com.viam.feeder.shared.WIFI_LIST_WITCH
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
    val getWifiList = sendEvent.asLiveTask {
        /* resourceMapper {
             if (it is LiveTaskError) {
                 LiveTaskResource.Error(it.exception)
             } else if (result() !is LiveTaskResource.Success) {
                 LiveTaskResource.Loading(it)
             } else {
                 LiveTaskResource.Success(it)
             }
         }*/
    }
    val addDeviceTask = addDevice.asLiveTask()
    val getWifiListTask = wifiList(Unit).asLiveData()
    val setWifiCredentialsTask = setWifiCredentials.asLiveTask {
        onSuccess<Any> {
            val parameter = getParameter()
            addDevice(parameter)
        }
    }

    val combinedTasks = combine(
        getWifiList,
        addDeviceTask,
        setWifiCredentialsTask
    )

    init {
        requestGetWifiList()
    }

    private fun addDevice(parameter: WifiAuthentication) = launchInScope {
        addDeviceTask(
            Device(
                1,
                "Device1",
                parameter.staticIp,
                parameter.port,
                parameter.gateway,
                parameter.subnet
            )
        )
        remoteConnectionConfig.url = parameter.staticIp ?: DEFAULT_ACCESS_POINT_IP
        remoteConnectionConfig.port = parameter.port ?: DEFAULT_ACCESS_POINT_PORT
    }

    private fun requestGetWifiList() = launchInScope {
        getWifiList(WIFI_LIST_WITCH)
        delay(5000)
        getWifiList(WIFI_LIST_WITCH)
        while (currentCoroutineContext().isActive) {
            getWifiList(WIFI_LIST_WITCH)
            delay(10000)
        }
    }

    fun onPasswordConfirmed(
        wifiDevice: WifiDevice,
        password: String,
        staticIp: String?,
        gateway: String?,
        subnet: String?
    ) = launchInScope {
        setWifiCredentialsTask(WifiAuthentication(wifiDevice.ssid, password, staticIp, gateway, subnet))
    }
}
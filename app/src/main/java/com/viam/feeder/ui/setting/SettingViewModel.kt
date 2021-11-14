package com.viam.feeder.ui.setting

import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.ViewModel
import com.part.livetaskcore.livatask.combine
import com.part.livetaskcore.usecases.asLiveTask
import com.viam.feeder.core.utility.launchInScope
import com.viam.feeder.data.datasource.RemoteConnectionConfig
import com.viam.feeder.domain.usecase.config.GetUseDhcp
import com.viam.feeder.domain.usecase.config.GetWifiGateway
import com.viam.feeder.domain.usecase.config.GetWifiIp
import com.viam.feeder.domain.usecase.config.GetWifiPassword
import com.viam.feeder.domain.usecase.config.GetWifiSsid
import com.viam.feeder.domain.usecase.config.GetWifiSubnet
import com.viam.feeder.domain.usecase.config.SetWifiCredentials
import com.viam.feeder.domain.usecase.config.WifiAuthentication
import com.viam.feeder.domain.usecase.device.AddDevice
import com.viam.feeder.model.Device
import com.viam.feeder.shared.DEFAULT_ACCESS_POINT_IP
import com.viam.feeder.shared.DEFAULT_ACCESS_POINT_PORT
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class SettingViewModel @Inject constructor(
    getWifiSsid: GetWifiSsid,
    getWifiIp: GetWifiIp,
    getUseDhcp: GetUseDhcp,
    getWifiGateway: GetWifiGateway,
    getWifiSubnet: GetWifiSubnet,
    getWifiPassword: GetWifiPassword,
    addDevice: AddDevice,
    setWifiCredentials: SetWifiCredentials,
    private val remoteConnectionConfig: RemoteConnectionConfig
) : ViewModel() {

    val useDhcp = MediatorLiveData<Boolean>()
    val wifiSsid = getWifiSsid()
    val wifiIp = getWifiIp()
    val wifiGateway = getWifiGateway()
    val wifiSubnet = getWifiSubnet()
    val wifiPassword = getWifiPassword()
    private val addDeviceTask = addDevice.asLiveTask {
        onSuccess<Any> {
            launchInScope {
                setWifiCredentialsTask.run()
            }
        }
    }
    private val setWifiCredentialsTask = setWifiCredentials.asLiveTask()

    val combinedTasks = combine(
        addDeviceTask,
        setWifiCredentialsTask
    ) {
        cancelable(true)
        retryable(true)
    }

    init {
        useDhcp.value = false
        useDhcp.addSource(getUseDhcp()) {
            useDhcp.postValue(it != 0)
        }
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

    fun onPasswordConfirmed(
        ssid: String,
        password: String,
        staticIp: String?,
        gateway: String?,
        subnet: String?,
        useStatic: Boolean
    ) = launchInScope {
        addDevice(WifiAuthentication(ssid, password, staticIp, gateway, subnet)).also {
            setWifiCredentialsTask.setParameter(
                WifiAuthentication(
                    ssid,
                    password,
                    staticIp,
                    gateway,
                    subnet,
                    useStatic = useStatic
                )
            )
        }
    }
}
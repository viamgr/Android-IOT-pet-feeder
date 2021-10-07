package com.viam.feeder.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import com.part.livetaskcore.livatask.combine
import com.part.livetaskcore.livatask.parametricLiveTask
import com.part.livetaskcore.usecases.asLiveTask
import com.viam.feeder.core.utility.launchInScope
import com.viam.feeder.data.datasource.RemoteConnectionConfig
import com.viam.feeder.di.NetWorkModule.Companion.API_IP
import com.viam.feeder.domain.usecase.config.GetConfig
import com.viam.feeder.domain.usecase.device.AddDevice
import com.viam.feeder.domain.usecase.device.GetConfiguredDevice
import com.viam.feeder.domain.usecase.device.HasPingFromIp
import com.viam.feeder.domain.usecase.device.HasPingFromIp.PingCheck
import com.viam.feeder.model.ConnectionType.DIRECT_AP
import com.viam.feeder.model.ConnectionType.OVER_ROUTER
import com.viam.feeder.model.ConnectionType.OVER_SERVER
import com.viam.feeder.model.Device
import com.viam.feeder.model.DeviceConnection
import com.viam.feeder.shared.ACCESS_POINT_SSID
import com.viam.feeder.shared.DEFAULT_ACCESS_POINT_IP
import com.viam.feeder.shared.DEFAULT_ACCESS_POINT_PORT
import com.viam.feeder.shared.DeviceConnectionException
import com.viam.resource.Resource
import com.viam.resource.Resource.Error
import com.viam.resource.Resource.Success
import com.viam.resource.dataOrNull
import com.viam.websocket.WebSocketApi
import dagger.hilt.android.lifecycle.HiltViewModel
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    getConfig: GetConfig,
    private val getConfiguredDevice: GetConfiguredDevice,
    private val hasPingFromIp: HasPingFromIp,
    private val webSocketApi: WebSocketApi,
    private val addDevice: AddDevice,
    private val remoteConnectionConfig: RemoteConnectionConfig
) : ViewModel() {
    val transferFileProgress = webSocketApi.progress.asLiveData()
    var askedWifiPermissions = AtomicBoolean(false)
    var isWifiDialogShowing: Boolean = false
    val getConfigTask = getConfig.asLiveTask {
        cancelable(true)
        withParameter(Unit)
    }
    val networkStatusCheckerLiveTask = parametricLiveTask<NetworkOptions, Resource<DeviceConnection>> {
        onSuccess<DeviceConnection> {
            onDeviceFound(it)
        }
        onRun {
            val networkOptions = getParameter()
            if (networkOptions.isAvailable) {
                val device = getConfiguredDevice()
                val deviceConnection =
                    isApConnection(networkOptions) ?: isConnectedOverRouter(device)
                    ?: isConnectedOverServer(device)

                emit(
                    if (deviceConnection != null) {
                        Success(deviceConnection)
                    } else {
                        Error(DeviceConnectionException("Can not connect to the device."))
                    }
                )
            } else {
                emit(Error(DeviceConnectionException("Disconnected from network.")))
            }
        }
    }

    private fun getConfigs() = launchInScope {
        getConfigTask.run()
    }

    private fun onDeviceFound(deviceConnection: DeviceConnection) {
        remoteConnectionConfig.url = deviceConnection.host
        webSocketApi.openWebSocket()
        getConfigs()
    }

    val combinedLiveTask = combine(getConfigTask, networkStatusCheckerLiveTask)
    private suspend fun isConnectedOverRouter(device: Device?): DeviceConnection? {
        if (device == null) return null
        val host = device.staticIp ?: return null
        val port = device.port
        val pingCheck = PingCheck(host, port)
        val hasPing = hasPingFromIp(pingCheck).dataOrNull() == true
        return if (hasPing) DeviceConnection(pingCheck.host, OVER_ROUTER) else null
    }

    private suspend fun isConnectedOverServer(device: Device?): DeviceConnection? {
        if (device == null) return null
        val pingCheck = PingCheck(API_IP)
        val hasPing = hasPingFromIp(pingCheck).dataOrNull() == true
        return if (hasPing) DeviceConnection(pingCheck.host, OVER_SERVER) else null
    }

    private suspend fun getConfiguredDevice(): Device? {
        val configuredDevice = getConfiguredDevice(Unit)
        return configuredDevice.dataOrNull()
    }

    private suspend fun isApConnection(networkOptions: NetworkOptions): DeviceConnection? {
        val isConnectedToAp = networkOptions.isAvailable && networkOptions.wifiName == ACCESS_POINT_SSID
        val pingCheck = PingCheck(DEFAULT_ACCESS_POINT_IP, DEFAULT_ACCESS_POINT_PORT)
        return if (isConnectedToAp || hasPingFromIp(pingCheck).dataOrNull() == true) {
            DeviceConnection(DEFAULT_ACCESS_POINT_IP, DIRECT_AP)
        } else null
    }

    fun onNetworkStatusChanged(networkOptions: NetworkOptions) = launchInScope {
        networkStatusCheckerLiveTask.setParameter(networkOptions).run()
    }

    data class NetworkOptions(val isAvailable: Boolean, val isWifi: Boolean, val wifiName: String?)
}


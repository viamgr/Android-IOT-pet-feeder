package com.viam.feeder.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.part.livetaskcore.livatask.combine
import com.part.livetaskcore.livatask.parametricLiveTask
import com.part.livetaskcore.usecases.asLiveTask
import com.viam.feeder.core.utility.launchInScope
import com.viam.feeder.data.datasource.RemoteConnectionConfig
import com.viam.feeder.di.NetWorkModule.Companion.API_IP
import com.viam.feeder.domain.usecase.config.GetConfig
import com.viam.feeder.domain.usecase.device.GetConfiguredDevice
import com.viam.feeder.domain.usecase.device.HasPingFromIp
import com.viam.feeder.domain.usecase.device.HasPingFromIp.PingCheck
import com.viam.feeder.domain.usecase.event.WebSocketEvents
import com.viam.feeder.model.ConnectionType.DIRECT_AP
import com.viam.feeder.model.ConnectionType.OVER_ROUTER
import com.viam.feeder.model.ConnectionType.OVER_SERVER
import com.viam.feeder.model.Device
import com.viam.feeder.model.DeviceConnection
import com.viam.feeder.shared.ACCESS_POINT_SSID
import com.viam.feeder.shared.DEFAULT_ACCESS_POINT_IP
import com.viam.feeder.shared.DEFAULT_ACCESS_POINT_PORT
import com.viam.feeder.shared.DeviceConnectionTimoutException
import com.viam.feeder.shared.NetworkNotAvailableException
import com.viam.resource.Resource
import com.viam.resource.Resource.Error
import com.viam.resource.Resource.Success
import com.viam.resource.dataOrNull
import com.viam.resource.isSuccess
import com.viam.websocket.WebSocketApi
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import okhttp3.Request
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject
import javax.inject.Provider

@HiltViewModel
class MainViewModel @Inject constructor(
    getConfig: GetConfig,
    private val getConfiguredDevice: GetConfiguredDevice,
    private val hasPingFromIp: HasPingFromIp,
    private val webSocketApi: WebSocketApi,
    private val webSocketEvents: WebSocketEvents,
    private val remoteConnectionConfig: RemoteConnectionConfig,
    private val request: Provider<Request>
) : ViewModel() {
    val transferFileProgress = webSocketApi.progress.asLiveData()
    var askedWifiPermissions = AtomicBoolean(false)
    var isWifiDialogShowing: Boolean = false
    val getConfigTask = getConfig.asLiveTask {
        cancelable(true)
        withParameter(Unit)
    }

    var retryTimeoutJob: Job? = null
    private fun retryTimeout() {
        retryTimeoutJob?.cancel()
        retryTimeoutJob = viewModelScope.launch {
            delay(5000)
            networkStatusCheckerLiveTask.retry()
        }
    }

    val networkStatusCheckerLiveTask = parametricLiveTask<NetworkOptions, Resource<DeviceConnection>> {
        onError {

            if (it is DeviceConnectionTimoutException && getParameter().isAvailable)
                retryTimeout()
        }
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

                if (deviceConnection != null) {
                    emit(Success(deviceConnection))
                } else {
                    emit(Error(DeviceConnectionTimoutException("Can not connect to the device.")))
                }
            } else {
                emit(Error(NetworkNotAvailableException("Network is not available")))
            }
        }
    }

    init {
        watchSocketEvent()
    }

    private fun watchSocketEvent() = launchInScope {
        webSocketEvents(Unit).collect {
            if (it.isSuccess()) {
                getConfigs()
            }
        }

    }

    private fun getConfigs() = launchInScope {
        getConfigTask.run()
    }

    private fun onDeviceFound(deviceConnection: DeviceConnection) = launchInScope {
        remoteConnectionConfig.url = deviceConnection.host
        webSocketApi.openWebSocket(request.get())
    }

    val combinedLiveTask = combine(getConfigTask, networkStatusCheckerLiveTask)
    private suspend fun isConnectedOverRouter(device: Device?): DeviceConnection? {
        if (device == null) return null
        val host = device.staticIp ?: return null
        val pingCheck = PingCheck(host)
        val hasPingFromIp1 = hasPingFromIp(pingCheck)
        val dataOrNull = hasPingFromIp1.dataOrNull()
        val hasPing = dataOrNull == true
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
        retryTimeoutJob?.cancel()
        networkStatusCheckerLiveTask(networkOptions)
    }

    data class NetworkOptions(val isAvailable: Boolean, val isWifi: Boolean, val wifiName: String?)
}


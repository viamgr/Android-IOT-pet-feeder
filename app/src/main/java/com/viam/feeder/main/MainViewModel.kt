package com.viam.feeder.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.part.livetaskcore.livatask.combine
import com.part.livetaskcore.usecases.asLiveTask
import com.viam.feeder.core.utility.launchInScope
import com.viam.feeder.data.datasource.RemoteConnectionConfig
import com.viam.feeder.domain.usecase.ConnectionStatus
import com.viam.feeder.domain.usecase.ConnectionStatus.NetworkOptions
import com.viam.feeder.domain.usecase.device.GetConfiguredDevice
import com.viam.feeder.domain.usecase.event.SocketSubscribe
import com.viam.feeder.model.Device
import com.viam.feeder.model.DeviceConnection
import com.viam.feeder.shared.DeviceConnectionTimoutException
import com.viam.resource.dataOrNull
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
    private val getConfiguredDevice: GetConfiguredDevice,
    private val connectionStatus: ConnectionStatus,
    private val webSocketApi: WebSocketApi,
    private val webSocketEvents: SocketSubscribe,
    private val remoteConnectionConfig: RemoteConnectionConfig,
    private val request: Provider<Request>
) : ViewModel() {

    val transferFileProgress = webSocketApi.progress.asLiveData()
    var askedWifiPermissions = AtomicBoolean(false)
    var isWifiDialogShowing: Boolean = false
    val webSocketEventsTask = webSocketEvents.asLiveTask {
        cancelable(false)
        withParameter(Unit)
    }

    var retryTimeoutJob: Job? = null

    val networkStatusCheckerLiveTask =
        connectionStatus.asLiveTask {
            cancelable(false)
            onError {
                if (it is DeviceConnectionTimoutException && getParameter().isAvailable)
                    retryTimeout()
            }
            onSuccess<DeviceConnection> {
                onDeviceFound(it)
            }
        }
    val combinedLiveTask = combine(webSocketEventsTask, networkStatusCheckerLiveTask) {
        cancelable(true)
        /*setLoadingText { data: Any? ->
             LoadingMessage.Res(R.string.loading, 10, "45")
             LoadingMessage.Res(R.string.loading)
             LoadingMessage.Text((data as List<Resource.Loading?>).joinToString(separator = ",") {
                 it?.data.toString()
             })
         }*/
    }

    private fun retryTimeout() {
        retryTimeoutJob?.cancel()
        retryTimeoutJob = viewModelScope.launch {
            delay(5000)
            networkStatusCheckerLiveTask.retry()
        }
    }

    init {
        watchSocketEvent()

        launchInScope {
            webSocketApi.events.collect {
                println("events $it")
            }
        }
    }

    private fun watchSocketEvent() = launchInScope {
        webSocketEventsTask(Unit)
    }

    private fun onDeviceFound(deviceConnection: DeviceConnection) = launchInScope {
        remoteConnectionConfig.url = deviceConnection.host
        webSocketApi.openWebSocket(request.get())
    }

    private suspend fun getConfiguredDevice(): Device? {
        val configuredDevice = getConfiguredDevice(Unit)
        return configuredDevice.dataOrNull()
    }

    fun onNetworkStatusChanged(networkOptions: NetworkOptions) = launchInScope {
        retryTimeoutJob?.cancel()
        networkStatusCheckerLiveTask(networkOptions)
    }
}


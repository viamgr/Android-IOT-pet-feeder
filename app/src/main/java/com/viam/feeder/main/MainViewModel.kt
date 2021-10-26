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
import com.viam.feeder.domain.usecase.config.GetConfig
import com.viam.feeder.domain.usecase.device.GetConfiguredDevice
import com.viam.feeder.domain.usecase.event.SendEvent
import com.viam.feeder.domain.usecase.event.SendStringValue
import com.viam.feeder.domain.usecase.event.WebSocketEvents
import com.viam.feeder.model.Device
import com.viam.feeder.model.DeviceConnection
import com.viam.feeder.model.KeyValueMessage
import com.viam.feeder.shared.DeviceConnectionTimoutException
import com.viam.feeder.shared.PAIR
import com.viam.feeder.shared.SUBSCRIBE
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
    private val connectionStatus: ConnectionStatus,
    private val webSocketApi: WebSocketApi,
    private val sendEvent: SendEvent,
    private val subscribe: SendEvent,
    private val sendStringValue: SendStringValue,
    private val webSocketEvents: WebSocketEvents,
    private val remoteConnectionConfig: RemoteConnectionConfig,
    private val request: Provider<Request>
) : ViewModel() {
    val subscribeTask = subscribe.asLiveTask {
        onSuccess<Any> {
            pair()
        }
    }
    val pairTask = sendStringValue.asLiveTask {
        onSuccess<Any> {
            getConfigs()
        }
    }
    val transferFileProgress = webSocketApi.progress.asLiveData()
    var askedWifiPermissions = AtomicBoolean(false)
    var isWifiDialogShowing: Boolean = false
    val getConfigTask = getConfig.asLiveTask {
        cancelable(false)
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

    init {
        watchSocketEvent()
    }

    private fun watchSocketEvent() = launchInScope {
        webSocketEvents(Unit).collect {
            if (it.isSuccess()) {
                subscribe()
            }
        }

    }

    private fun pair() = launchInScope {
        pairTask(KeyValueMessage(PAIR, "Feeder1"))
    }

    private fun subscribe() = launchInScope {
        subscribeTask(SUBSCRIBE)
    }

    private fun getConfigs() = launchInScope {
        getConfigTask.run()
    }

    private fun onDeviceFound(deviceConnection: DeviceConnection) = launchInScope {
        remoteConnectionConfig.url = deviceConnection.host
        webSocketApi.openWebSocket(request.get())
    }

    val combinedLiveTask = combine(getConfigTask, networkStatusCheckerLiveTask)

    private suspend fun getConfiguredDevice(): Device? {
        val configuredDevice = getConfiguredDevice(Unit)
        return configuredDevice.dataOrNull()
    }

    fun onNetworkStatusChanged(networkOptions: NetworkOptions) = launchInScope {
        retryTimeoutJob?.cancel()
        networkStatusCheckerLiveTask(networkOptions)
    }
}


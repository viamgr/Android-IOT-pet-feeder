package com.viam.feeder.domain.usecase

import com.viam.feeder.domain.base.CoroutinesDispatcherProvider
import com.viam.feeder.domain.base.FlowUseCase
import com.viam.feeder.domain.repositories.socket.DeviceRepository
import com.viam.feeder.domain.usecase.ConnectionStatus.NetworkOptions
import com.viam.feeder.model.ConnectionType.*
import com.viam.feeder.model.Device
import com.viam.feeder.model.DeviceConnection
import com.viam.feeder.shared.*
import com.viam.resource.Resource
import com.viam.resource.Resource.Error
import com.viam.resource.Resource.Success
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import javax.inject.Inject

class ConnectionStatus @Inject constructor(
    coroutinesDispatcherProvider: CoroutinesDispatcherProvider,
    private val deviceRepository: DeviceRepository,
) : FlowUseCase<NetworkOptions, DeviceConnection>(coroutinesDispatcherProvider.io) {

    override fun execute(parameter: NetworkOptions): Flow<Resource<DeviceConnection>> =
        channelFlow {
            if (parameter.isAvailable) {
                val device = deviceRepository.getAll().firstOrNull()
                val deferred = coroutineScope {
                    val scope = currentCoroutineContext()
                    async {
                        isApConnection(parameter)?.let {
                            send(Success(it))
                            scope.cancelChildren()
                        }
                    }
                    async {
                        isConnectedOverRouter(parameter, device)?.let {
                            send(Success(it))
                            scope.cancelChildren()
                        }
                    }
                    async {
                        isConnectedOverServer(device)?.let {
                            send(Success(it))
                            scope.cancelChildren()
                        }
                    }
                    async {
                        delay(5000)
                        send(Error(DeviceConnectionTimoutException("Can not connect to the device.")))
                    }
                }
                deferred.join()
            } else {
                send(Error(NetworkNotAvailableException("Network is not available")))
            }
        }

    private fun isConnectedOverServer(device: Device?): DeviceConnection? {
//        if (device == null) return null
        val hasPing = hasPingFromIp(API_IP, 5000)
        return if (hasPing) DeviceConnection(API_IP, OVER_SERVER) else null
    }

    private fun isConnectedOverRouter(
        networkOptions: NetworkOptions,
        device: Device?
    ): DeviceConnection? {
        if (device == null || !networkOptions.isWifi) return null
        val host = device.staticIp ?: return null
        return if (hasPingFromIp(host, 5000)) DeviceConnection(host, OVER_ROUTER) else null
    }


    private fun isApConnection(networkOptions: NetworkOptions): DeviceConnection? {
        val isConnectedToAp = networkOptions.wifiName == ACCESS_POINT_SSID
        return if (isConnectedToAp || hasPingFromIp(DEFAULT_ACCESS_POINT_IP, 5000)) {
            DeviceConnection(DEFAULT_ACCESS_POINT_IP, DIRECT_AP)
        } else null
    }

    data class NetworkOptions(val isAvailable: Boolean, val isWifi: Boolean, val wifiName: String?)
}
package com.viam.feeder.domain.usecase

import com.viam.feeder.domain.base.CoroutinesDispatcherProvider
import com.viam.feeder.domain.base.FlowUseCase
import com.viam.feeder.domain.repositories.socket.DeviceRepository
import com.viam.feeder.domain.usecase.ConnectionStatus.NetworkOptions
import com.viam.feeder.model.ConnectionType.DIRECT_AP
import com.viam.feeder.model.ConnectionType.OVER_ROUTER
import com.viam.feeder.model.ConnectionType.OVER_SERVER
import com.viam.feeder.model.Device
import com.viam.feeder.model.DeviceConnection
import com.viam.feeder.shared.ACCESS_POINT_SSID
import com.viam.feeder.shared.API_IP
import com.viam.feeder.shared.DEFAULT_ACCESS_POINT_IP
import com.viam.feeder.shared.DeviceConnectionTimoutException
import com.viam.feeder.shared.NetworkNotAvailableException
import com.viam.resource.Resource
import com.viam.resource.Resource.Error
import com.viam.resource.Resource.Success
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.async
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.ProducerScope
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.take
import javax.inject.Inject

class ConnectionStatus @Inject constructor(
    coroutinesDispatcherProvider: CoroutinesDispatcherProvider,
    private val deviceRepository: DeviceRepository,
) : FlowUseCase<NetworkOptions, DeviceConnection>(coroutinesDispatcherProvider.io) {

    override fun execute(parameter: NetworkOptions): Flow<Resource<DeviceConnection>> =
        channelFlow {
            if (parameter.isAvailable) {
                try {
                    getCorrectIpAddress(parameter)
                } catch (e: CancellationException) {
                    e.printStackTrace()
                }
            } else {
                send(Error(NetworkNotAvailableException("Network is not available")))
            }
        }.onEach {
            println(it)
        }.take(1)

    private suspend fun ProducerScope<Resource<DeviceConnection>>.getCorrectIpAddress(
        parameter: NetworkOptions
    ) {

        coroutineScope {

            val device = deviceRepository.getAll().firstOrNull()
            val scope = currentCoroutineContext()

            async {
                isApConnection(parameter, device)?.let {
                    send(Success(it))
                    scope.cancel()
                }
            }

            async {
                isConnectedOverRouter(parameter, device)?.let {
                    println("connect to router")
                    println(it)
                    send(Success(it))
                    scope.cancel()
                }
            }
            async {
                isConnectedOverServer(device)?.let {
                    println("connect to server")
                    send(Success(it))
                    scope.cancel()
                }
            }
            async {
                delay(15000)
                send(Error(DeviceConnectionTimoutException("Can not connect to the device.")))
                scope.cancel()
            }
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
        if (device == null || !networkOptions.isWifi || device.useDhcp == true) return null
        val host = device.staticIp ?: return null
        return if (hasPingFromIp(host, 5000)) DeviceConnection(host, OVER_ROUTER) else null
    }

    private fun isApConnection(networkOptions: NetworkOptions, device: Device?): DeviceConnection? {
        val isConnectedToAp = networkOptions.wifiName == ACCESS_POINT_SSID || networkOptions.wifiName == "AndroidWifi"
        val host = if(networkOptions.wifiName == "AndroidWifi"){
            DEFAULT_ACCESS_POINT_IP
        } else networkOptions.localIp ?: DEFAULT_ACCESS_POINT_IP

        return if (/*device == null && */(isConnectedToAp && hasPingFromIp(host, 5000))) {
            DeviceConnection(host, DIRECT_AP)
        } else null
    }

    data class NetworkOptions(
        val isAvailable: Boolean,
        val isWifi: Boolean,
        val wifiName: String?,
        val localIp: String?
    )
}
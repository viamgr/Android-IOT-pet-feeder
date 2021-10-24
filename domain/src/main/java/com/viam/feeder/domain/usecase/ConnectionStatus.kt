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
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.net.InetAddress
import javax.inject.Inject

class ConnectionStatus @Inject constructor(
    coroutinesDispatcherProvider: CoroutinesDispatcherProvider,
    private val deviceRepository: DeviceRepository,
) : FlowUseCase<NetworkOptions, DeviceConnection>(coroutinesDispatcherProvider.io) {

    override fun execute(parameter: NetworkOptions): Flow<Resource<DeviceConnection>> = flow {
        if (parameter.isAvailable) {
            val device = deviceRepository.getAll().firstOrNull()
            val deviceConnection =
                isApConnection(parameter) ?: isConnectedOverRouter(device)
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

    private fun isConnectedOverServer(device: Device?): DeviceConnection? {
        if (device == null) return null
        val hasPing = hasPingFromIp(API_IP, 5000)
        return if (hasPing) DeviceConnection(API_IP, OVER_SERVER) else null
    }

    private fun isConnectedOverRouter(device: Device?): DeviceConnection? {
        if (device == null) return null
        val host = device.staticIp ?: return null
        return if (hasPingFromIp(host, 5000)) DeviceConnection(host, OVER_ROUTER) else null
    }

    private fun hasPingFromIp(host: String, timeout: Int): Boolean {
        return try {
            val inetAddress = InetAddress.getByName(host)
            inetAddress!!.isReachable(timeout)
        } catch (e: Exception) {
            e.printStackTrace()
            throw e
        }
    }

    private fun isApConnection(networkOptions: NetworkOptions): DeviceConnection? {
        val isConnectedToAp = networkOptions.isAvailable && networkOptions.wifiName == ACCESS_POINT_SSID
        return if (isConnectedToAp || hasPingFromIp(DEFAULT_ACCESS_POINT_IP, 5000)) {
            DeviceConnection(DEFAULT_ACCESS_POINT_IP, DIRECT_AP)
        } else null
    }

    data class NetworkOptions(val isAvailable: Boolean, val isWifi: Boolean, val wifiName: String?)
}
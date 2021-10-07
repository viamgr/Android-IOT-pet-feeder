package com.viam.feeder.domain.usecase.device

import com.viam.feeder.domain.base.CoroutinesDispatcherProvider
import com.viam.feeder.domain.base.UseCase
import com.viam.feeder.domain.usecase.device.HasPingFromIp.PingCheck
import java.io.IOException
import java.net.InetAddress
import java.net.InetSocketAddress
import java.net.Socket
import javax.inject.Inject

class HasPingFromIp @Inject constructor(
    coroutinesDispatcherProvider: CoroutinesDispatcherProvider,
    val socket: Socket,
) : UseCase<PingCheck, Boolean>(coroutinesDispatcherProvider.io) {
    override suspend fun execute(parameters: PingCheck): Boolean {
        return isHostAvailable(parameters.host, parameters.port ?: 80, parameters.timeout ?: 5000)
    }

    /**
     * Check if host is reachable.
     *
     * @param host    The host to check for availability. Can either be a machine name, such as "google.com",
     * or a textual representation of its IP address, such as "8.8.8.8".
     * @param port    The port number.
     * @param timeout The timeout in milliseconds.
     * @return True if the host is reachable. False otherwise.
     */
    private fun isHostAvailable(host: String, port: Int, timeout: Int): Boolean {
        try {
            socket.use { socket ->
                val inetAddress = InetAddress.getByName(host)
                val inetSocketAddress = InetSocketAddress(inetAddress, port)
                socket.connect(inetSocketAddress, timeout)
                return true
            }
        } catch (e: IOException) {
            e.printStackTrace()
            return false
        }
    }

    data class PingCheck(val host: String, val port: Int? = 80, val timeout: Int? = 5000)
}
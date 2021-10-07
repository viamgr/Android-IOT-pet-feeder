package com.viam.feeder.domain.usecase.device

import com.viam.feeder.domain.base.CoroutinesDispatcherProvider
import com.viam.feeder.domain.base.UseCase
import com.viam.feeder.domain.usecase.device.HasPingFromIp.PingCheck
import java.net.InetAddress
import java.net.Socket
import javax.inject.Inject

class HasPingFromIp @Inject constructor(
    coroutinesDispatcherProvider: CoroutinesDispatcherProvider,
    val socket: Socket,
) : UseCase<PingCheck, Boolean>(coroutinesDispatcherProvider.io) {
    override suspend fun execute(parameters: PingCheck): Boolean {
        return executeCommand(parameters.host, parameters.timeout ?: 5000)
    }

    private fun executeCommand(host: String, timeout: Int): Boolean {
        return try {
            val inetAddress = InetAddress.getByName(host)
            inetAddress!!.isReachable(timeout)
        } catch (e: Exception) {
            e.printStackTrace()
            throw e
        }
    }

    data class PingCheck(val host: String, val timeout: Int? = 5000)
}
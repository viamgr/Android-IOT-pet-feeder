@file:JvmName(name = "PingUtils")

package com.viam.feeder.domain.usecase

import java.net.InetAddress

fun hasPingFromIp(host: String, timeout: Int): Boolean {
    return try {
        val inetAddress = InetAddress.getByName(host)

        println("host $host")
        inetAddress!!.isReachable(timeout)
    } catch (e: Exception) {
        e.printStackTrace()
        throw e
    }
}
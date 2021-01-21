package com.viam.feeder.data.utils

import android.content.Context
import com.viam.feeder.constants.ACCESS_POINT_SSID
import com.viam.feeder.ui.wifi.Connectivity.getWifiName
import kotlinx.coroutines.delay
import java.net.ConnectException
import kotlin.random.Random

suspend inline fun <T> fakeRequest(
    context: Context,
    delayTime: Int = 2500,
    possibility: Int = 30,
    crossinline body: suspend () -> T
): T {
    if (context.getWifiName() != ACCESS_POINT_SSID) {
        throw ConnectException()
    }
    delay(Random.nextInt(0, delayTime).toLong())
    return when (Random.nextInt(0, possibility)) {
        0, 1, 2 -> {
            throw Exception("Random Error")
        }
        else -> {
            body()
        }
    }
}
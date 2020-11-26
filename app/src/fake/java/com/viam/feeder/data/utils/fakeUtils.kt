package com.viam.feeder.data.utils

import com.viam.feeder.ui.wifi.ConnectionUtil
import kotlinx.coroutines.delay
import java.net.ConnectException
import kotlin.random.Random

suspend inline fun <T> fakeRequest(
    delayTime: Int = 2500,
    possibility: Int = 30,
    crossinline body: suspend () -> T
): T {
    if (ConnectionUtil.connectionState.value?.isAvailable == false) {
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
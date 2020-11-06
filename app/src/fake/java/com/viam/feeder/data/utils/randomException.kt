package com.viam.feeder.data.utils

import kotlinx.coroutines.delay
import java.net.ConnectException
import kotlin.random.Random

suspend inline fun <T> randomException(
    delayTime: Int = 5000,
    possibility: Int = 10000,
    crossinline body: suspend () -> T
): T {
    delay(Random.nextInt(0, delayTime).toLong())
    return when (Random.nextInt(0, possibility)) {
        0 -> {
            throw Exception("Random Error")
        }
        1, 2, 3 -> {
            throw ConnectException()
        }
        else -> {
            body()
        }
    }
}
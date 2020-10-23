package com.viam.feeder.core.extensions

import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.supervisorScope

suspend fun Long.timerAsync(action: (count: Long) -> Unit) = supervisorScope {
    var count = 0L
    return@supervisorScope launchPeriodicAsync(1000) {
        if (count == this@timerAsync) {
            action(count)
            cancel()
        } else {
            action(count)
        }
        count++
    }
}

suspend fun launchPeriodicAsync(
    repeatMillis: Long,
    action: () -> Unit
) = supervisorScope {
    if (repeatMillis > 0) {
        while (isActive) {
            action()
            delay(repeatMillis)
        }
    } else {
        action()
    }
}
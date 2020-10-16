package com.viam.feeder.core.extensions

import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.supervisorScope

suspend fun Long.timerAsync(action: (count: Long) -> Unit) = supervisorScope {
    var count = 0L
    action(count)
    return@supervisorScope launchPeriodicAsync(1000) {
        if (count++ == this@timerAsync - 1) {
            this.cancel()
        }
        action(count)
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
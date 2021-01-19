package com.viam.feeder.core.domain.utils

import android.content.Context
import com.viam.feeder.R
import com.viam.feeder.constants.ACCESS_POINT_SSID
import com.viam.feeder.core.task.CompositeException
import java.net.ConnectException

fun Throwable.toMessage(context: Context): String {
    return when {
        this is CompositeException -> {
            this.errors.map { it.toMessage(context) }.distinct().joinToString("\n")
        }
        isConnectionError() -> {
            context.getString(R.string.wrong_connected, ACCESS_POINT_SSID)
        }
        else -> {
            context.getString(R.string.error_happened)
        }
    }
}

fun Throwable.isConnectionError(): Boolean {
    return this is ConnectException
}
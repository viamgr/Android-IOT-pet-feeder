package com.viam.feeder.core.domain

import android.content.Context
import com.viam.feeder.R
import com.viam.feeder.core.task.CompositeException
import java.net.ConnectException

fun Throwable.toMessage(context: Context): String {
    return if (this is CompositeException) {
        this.errors.map { it.toMessage(context) }.distinct().joinToString("\n")
    } else if (isConnectionError()) {
        context.getString(R.string.wrong_connected)
    } else {
        context.getString(R.string.error_happened)
    }
}

fun Throwable.isConnectionError(): Boolean {
    return this is ConnectException
}
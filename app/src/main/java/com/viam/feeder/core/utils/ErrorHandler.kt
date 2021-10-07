package com.viam.feeder.core.utils

import android.content.Context
import com.part.livetaskcore.livatask.CombinedException
import com.part.livetaskcore.livatask.ViewException
import com.viam.feeder.R
import com.viam.feeder.shared.ACCESS_POINT_SSID
import com.viam.feeder.shared.DeviceConnectionException
import com.viam.websocket.SocketCloseException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow
import java.net.ConnectException

fun Throwable.toMessage(context: Context): String {
    return when {
        isConnectionError() -> {
            context.getString(R.string.wrong_connected, ACCESS_POINT_SSID)
        }
        else -> {
            context.getString(R.string.error_happened)
        }
    }
}

fun Throwable.isConnectionError(): Boolean {
    return this is ConnectException ||
        this is DeviceConnectionException ||
        this is SocketCloseException ||
        this.cause is SocketCloseException ||
        (this is ViewException && this.cause is SocketCloseException) ||
        this.cause is DeviceConnectionException ||
        (this is ViewException && this.cause is DeviceConnectionException) ||
        (this is CombinedException && this.exceptions.any {
            it.isConnectionError()
        })
}

fun <T> Flow<T>.catchFlow(): Flow<Result<T>> = flow {
    try {
        collect { value ->
            emit(Result.success(value))
        }
    } catch (e: Throwable) {
        if (e !is CancellationException) {
            emit(Result.failure<T>(e))
        }
    }
}
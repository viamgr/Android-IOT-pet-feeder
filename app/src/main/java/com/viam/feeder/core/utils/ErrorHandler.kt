package com.viam.feeder.core.utils

import android.content.Context
import com.part.livetaskcore.livatask.CombinedException
import com.viam.feeder.R
import com.viam.feeder.shared.DeviceConnectionTimoutException
import com.viam.feeder.shared.NetworkNotAvailableException
import com.viam.websocket.SocketCloseException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow
import java.net.ConnectException

fun Throwable.toMessage(context: Context): String {
    return when {
        this is NetworkNotAvailableException -> {
            context.getString(R.string.network_is_not_available)
        }
        this.cause is SocketCloseException -> {
            context.getString(R.string.socket_close_error)
        }
        isConnectionError() -> {
            context.getString(R.string.wrong_connected)
        }
        else -> {
            context.getString(R.string.error_happened)
        }
    }
}

fun Throwable.isConnectionError(): Boolean {
    return this is ConnectException ||
        this is DeviceConnectionTimoutException ||
        this is SocketCloseException ||
        this.cause is SocketCloseException ||
        this.cause is DeviceConnectionTimoutException ||
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
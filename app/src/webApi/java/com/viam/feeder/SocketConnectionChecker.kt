package com.viam.feeder

import com.part.livetaskcore.NoConnectionInformer
import com.viam.websocket.SocketCloseException
import com.viam.websocket.WebSocketApi
import javax.inject.Inject

class SocketConnectionChecker @Inject constructor(webSocketApi: WebSocketApi) :
    NoConnectionInformer({ callback ->
        webSocketApi.setOnConnectionChangedListener { isConnected ->
            if (isConnected) {
                callback()
            }
        }
    }) {
    override fun isRetryable(throwable: Throwable) = throwable is SocketCloseException
}
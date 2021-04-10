package com.part.livetaskcore

import com.part.livetaskcore.connection.ConnectionManager

class WebConnectionChecker(connectionManager: ConnectionManager) :
    NoConnectionInformer({ callback ->
        connectionManager.setOnStatusChangeListener { isConnected ->
            if (isConnected) {
                callback()
            }
        }
    }) {
    override fun isRetryable(throwable: Throwable) = throwable.message.toString()
        .startsWith("Unable to resolve host")
}
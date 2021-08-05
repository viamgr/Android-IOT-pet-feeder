package com.part.livetaskcore.connection

class WebConnectionChecker(connectionManager: ConnectionManager) :
    BaseConnectionInformer({ callback ->
        connectionManager.setOnStatusChangeListener { isConnected ->
            if (isConnected) {
                callback()
            }
        }
    }) {
    override fun isRetryable(throwable: Throwable) = throwable.message.toString()
        .startsWith("Unable to resolve host")
}
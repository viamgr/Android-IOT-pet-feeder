package com.part.livetaskcore

interface ErrorObserverCallback {
    fun notifyError(errorEvent: ErrorEvent)
}
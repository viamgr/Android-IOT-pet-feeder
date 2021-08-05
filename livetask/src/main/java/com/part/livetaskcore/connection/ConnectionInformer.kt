package com.part.livetaskcore.connection

import com.part.livetaskcore.livatask.LiveTask

interface ConnectionInformer {

    fun isRetryable(throwable: Throwable): Boolean
    fun register(throwable: Throwable, liveTask: LiveTask<*>): Boolean
    fun retryFailed(): Boolean
    fun unregister(liveTask: LiveTask<*>)
}

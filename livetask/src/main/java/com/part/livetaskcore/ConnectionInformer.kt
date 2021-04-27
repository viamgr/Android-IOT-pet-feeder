package com.part.livetaskcore

import com.part.livetaskcore.livatask.LiveTask

interface ConnectionInformer {

    fun isRetryable(throwable: Throwable): Boolean
    fun registerIfRetryable(throwable: Throwable, liveTask: LiveTask<*>): Boolean
    fun retryFailed(): Boolean
    fun unregister(liveTask: LiveTask<*>)
}

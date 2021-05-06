package com.part.livetaskcore

import com.part.livetaskcore.livatask.LiveTask

class MultipleConnectionInformer(private vararg val informers: BaseConnectionInformer) :
    ConnectionInformer {
    override fun isRetryable(throwable: Throwable): Boolean {
        return informers.any { it.isRetryable(throwable) }
    }

    override fun register(throwable: Throwable, liveTask: LiveTask<*>): Boolean {
        return informers.any { it.register(throwable, liveTask) }
    }

    override fun retryFailed(): Boolean {
        return informers.any { it.retryFailed() }
    }

    override fun unregister(liveTask: LiveTask<*>) {
        informers.forEach { it.unregister(liveTask) }
    }

}
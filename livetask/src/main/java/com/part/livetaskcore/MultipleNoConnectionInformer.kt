package com.part.livetaskcore

import com.part.livetaskcore.livatask.LiveTask

class MultipleNoConnectionInformer(private vararg val informers: NoConnectionInformer) :
    NoConnectionInformerAAA {
    override fun isRetryable(throwable: Throwable): Boolean {
        return informers.any { it.isRetryable(throwable) }
    }

    override fun registerIfRetryable(throwable: Throwable, liveTask: LiveTask<*>): Boolean {
        return informers.any { it.registerIfRetryable(throwable, liveTask) }
    }

    override fun retryFailed(): Boolean {
        return informers.any { it.retryFailed() }
    }

    override fun unregister(liveTask: LiveTask<*>) {
        informers.forEach { it.unregister(liveTask) }
    }

}
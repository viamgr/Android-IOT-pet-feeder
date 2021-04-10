package com.part.livetaskcore

import com.part.livetaskcore.livatask.LiveTask

class MultipleNoConnectionInformer(private vararg val informers: NoConnectionInformer) :
    NoConnectionInformer({ }) {
    override fun isRetryable(throwable: Throwable): Boolean {
        return informers.any { it.isRetryable(throwable) }
    }

    override fun registerIfRetryable(throwable: Throwable, liveTask: LiveTask<*>): Boolean {
        return informers.any { it.registerIfRetryable(throwable, liveTask) }
    }

    override fun retryFailed(): Boolean {
        return informers.any { it.retryFailed() }
    }

}
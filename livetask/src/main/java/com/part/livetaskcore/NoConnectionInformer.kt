package com.part.livetaskcore

import com.part.livetaskcore.livatask.LiveTask

typealias ConnectionCallback = (callback: () -> Unit) -> Unit

abstract class NoConnectionInformer(callback: ConnectionCallback) {
    init {
        callback.invoke {
            retryFailed()
        }
    }

    private val registers = mutableListOf<Pair<LiveTask<*>, Throwable>>()

    open fun registerIfRetryable(throwable: Throwable, liveTask: LiveTask<*>): Boolean {
        return if (isRetryable(throwable))
            registers.add(Pair(liveTask, throwable))
        else false
    }

    abstract fun isRetryable(throwable: Throwable): Boolean

    open fun unregister(liveTask: LiveTask<*>) {
        registers.remove(liveTask)
    }

    open fun retryFailed(): Boolean {
        return registers.firstOrNull {
            isRetryable(it.second)
        }?.let {
            unregister(it.first)
            it.first.retry()
            true
        } ?: false
    }
}
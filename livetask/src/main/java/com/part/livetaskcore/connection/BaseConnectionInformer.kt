package com.part.livetaskcore.connection

import com.part.livetaskcore.livatask.LiveTask

typealias ConnectionCallback = (callback: () -> Unit) -> Unit

abstract class BaseConnectionInformer(callback: ConnectionCallback) : ConnectionInformer {
    init {
        callback.invoke {
            retryFailed()
        }
    }

    private val registers = mutableListOf<Pair<LiveTask<*>, Throwable>>()

    override fun register(throwable: Throwable, liveTask: LiveTask<*>): Boolean {
        return if (isRetryable(throwable))
            registers.add(Pair(liveTask, throwable))
        else false
    }

    override fun unregister(liveTask: LiveTask<*>) {
        registers.firstOrNull { it.first == liveTask }?.let {
            registers.remove(it)
        }

    }

    override fun retryFailed(): Boolean {

        return registers.firstOrNull {
            isRetryable(it.second)
        }?.let {
            unregister(it.first)
            it.first.retry()
            true
        } ?: false
    }
}

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
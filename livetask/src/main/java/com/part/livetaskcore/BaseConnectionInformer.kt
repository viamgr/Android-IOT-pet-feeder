package com.part.livetaskcore

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
        println("unregister:${registers.size} hashCode:${this@BaseConnectionInformer.hashCode()}")

    }

    override fun retryFailed(): Boolean {
        println("retryFailed register:${registers.size}  hashCode:${this@BaseConnectionInformer.hashCode()}")

        return registers.firstOrNull {
            isRetryable(it.second)
        }?.let {
            unregister(it.first)
            it.first.retry()
            true
        } ?: false
    }
}
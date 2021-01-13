package com.viam.feeder.core.task

import com.viam.feeder.core.Resource

interface LiveTaskScope<P, R> {
    suspend fun emit(resource: Resource<R>?)
    fun onSuccess(block: (resource: R?) -> Unit): LiveTask<P, R>
    fun debounce(timeInMillis: Long)
    fun initialParams(params: P)
    fun cancelable(cancelable: Boolean = true)
    fun params(): P?
}

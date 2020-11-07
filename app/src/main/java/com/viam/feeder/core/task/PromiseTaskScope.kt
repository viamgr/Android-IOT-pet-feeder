package com.viam.feeder.core.task

import com.viam.feeder.core.Resource

interface PromiseTaskScope<P, R> {
    suspend fun emit(resource: Resource<R>?)
    fun onSuccess(block: (resource: R?) -> Unit)
    fun debounce(timeInMillis: Long)
    fun initialParams(params: P)
}

package com.viam.feeder.core.task

import com.viam.resource.Resource

interface LiveTaskScope<P, R> {
    suspend fun emit(resource: Resource<R>?)
    fun onSuccess(block: (resource: R?) -> Unit): LiveTask<P, R>
    fun initialParams(params: P)
    fun cancelable(cancelable: Boolean = true)
    fun params(): P?
}

package com.viam.feeder.core.task

import com.viam.feeder.core.Resource

interface PromiseTaskScope<R, T> {
    suspend fun emit(resource: Resource<T>?)
}

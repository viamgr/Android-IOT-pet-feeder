package com.viam.feeder.core.task

import com.viam.feeder.core.Resource

interface PromiseTaskScope<P, R> {
    suspend fun emit(resource: Resource<R>?)
}

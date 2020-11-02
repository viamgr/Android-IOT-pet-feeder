package com.viam.feeder.core.task

import com.viam.feeder.core.Resource

interface TaskBuilder<R, T> {
    suspend fun execute(block: suspend (R) -> Resource<T>)
    fun initialParams(params: R)
}
package com.viam.feeder.core.task

import androidx.lifecycle.LiveData
import com.viam.feeder.core.Resource

interface PromiseTask<R, T> {
    fun result(): LiveData<PromiseTask<R, T>?>
    fun status(): Resource<*>?
    fun cancel()
    fun retry()
    fun request(params: R)
    suspend fun logger(logger: EventLogger)
}

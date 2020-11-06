package com.viam.feeder.core.task

import androidx.lifecycle.MediatorLiveData
import com.viam.feeder.core.Resource

interface LiveTask<P, R> {
    fun execute(params: P)
    fun state(): Resource<R>?
    fun asLiveData(): MediatorLiveData<LiveTask<P, R>>
    fun cancel()
    fun retry()
    fun onSuccess(block: (resource: R?) -> Unit)
}
package com.viam.feeder.core.task

import androidx.lifecycle.MediatorLiveData
import com.viam.resource.Resource

interface LiveTask<P, R> {
    fun execute(params: P? = null): LiveTask<P, R>
    fun postWithCancel(params: P? = null)
    fun state(): Resource<R>?
    fun asLiveData(): MediatorLiveData<LiveTask<P, R>>
    fun cancel()
    fun retry()
    fun isCancelable(): Boolean
}
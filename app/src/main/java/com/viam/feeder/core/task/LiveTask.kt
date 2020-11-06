package com.viam.feeder.core.task

import androidx.lifecycle.LiveData
import com.viam.feeder.core.Resource

interface LiveTask<R, T> {
    fun execute(params: R)
    fun state(): Resource<T>?
    fun asLiveData(): LiveData<LiveTask<R, T>>?
    fun cancel()
    fun retry()
}
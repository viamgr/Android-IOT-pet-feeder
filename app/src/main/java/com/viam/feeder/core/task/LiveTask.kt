package com.viam.feeder.core.task

import androidx.lifecycle.LiveData
import com.viam.feeder.core.Resource

interface LiveTask<P, R> {
    fun execute(params: P)
    fun state(): Resource<R>?
    fun asLiveData(): LiveData<LiveTask<P, R>>?
    fun cancel()
    fun retry()
}
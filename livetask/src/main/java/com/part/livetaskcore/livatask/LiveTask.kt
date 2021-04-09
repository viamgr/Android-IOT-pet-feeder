package com.part.livetaskcore.livatask

import androidx.lifecycle.LiveData
import com.part.livetaskcore.bindingadapter.ProgressType
import com.viam.resource.Resource
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

interface LiveTask<T> {
    var loadingViewType: ProgressType
    fun result(): Resource<T>?
    fun asLiveData(): LiveData<LiveTask<T>>
    fun retry()
    suspend fun run(): LiveTask<T>
    fun runOn(coroutineContext: CoroutineContext? = EmptyCoroutineContext): LiveTask<T>
    fun cancel()
}

interface ParameterLiveTask<P, T> : LiveTask<T> {
    suspend operator fun invoke(parameter: P): LiveTask<T>
    fun getParameter(): P
    fun setParameter(parameter: P): LiveTask<T>
}

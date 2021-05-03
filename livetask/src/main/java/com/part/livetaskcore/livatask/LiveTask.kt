package com.part.livetaskcore.livatask

import androidx.lifecycle.LiveData
import com.part.livetaskcore.Resource
import com.part.livetaskcore.bindingadapter.ViewType
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

interface LiveTask<T> {
    val isRetryable: Boolean?
    val isAutoRetry: Boolean?
    val isCancelable: Boolean?
    var loadingViewType: ViewType
    fun result(): Resource<T>?
    fun asLiveData(): LiveData<LiveTask<T>>
    fun retry()
    suspend fun run(): LiveTask<T>
    fun run(coroutineContext: CoroutineContext? = EmptyCoroutineContext): LiveTask<T>
    fun cancel()
}

interface ParametricLiveTask<P, T> : LiveTask<T> {
    suspend operator fun invoke(parameter: P): ParametricLiveTask<P, T>
    fun getParameter(): P
    fun setParameter(parameter: P): ParametricLiveTask<P, T>
}

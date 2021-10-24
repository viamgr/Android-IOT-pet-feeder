package com.part.livetaskcore.livatask

import androidx.lifecycle.LiveData
import com.part.livetaskcore.ErrorMapper
import com.part.livetaskcore.Resource
import com.part.livetaskcore.views.ViewType
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

interface LiveTask<T> {
    fun isRetryable(): Boolean
    fun isAutoRetry(): Boolean
    fun isCancelable(): Boolean
    fun loadingViewType(): ViewType?
    val liveResult: LiveData<Resource<T>?>
    fun result(): Resource<T>?
    fun errorMapper(): ErrorMapper
    fun asLiveData(): LiveData<LiveTask<T>>
    fun retry()
    suspend fun run(): LiveTask<T>
    fun run(coroutineContext: CoroutineContext = EmptyCoroutineContext): LiveTask<T>
    fun cancel(immediately: Boolean? = true): LiveTask<T>
    fun configure()
}

interface ParametricLiveTask<P, T> : LiveTask<T> {
    suspend operator fun invoke(parameter: P): ParametricLiveTask<P, T>
    fun getParameter(): P
    fun setParameter(parameter: P): ParametricLiveTask<P, T>
}

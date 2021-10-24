package com.part.livetaskcore.livatask

import androidx.lifecycle.LiveData
import com.part.livetaskcore.ErrorMapper
import com.part.livetaskcore.Resource
import com.part.livetaskcore.ResourceMapper
import com.part.livetaskcore.views.ViewType
import kotlinx.coroutines.DisposableHandle

/**
 * Interface that allows controlling a LiveTask from a coroutine block.
 */

interface LiveTaskBuilder<T> {
    fun emitResult(resultBlock: EmitResultBlock<T>)
    fun emitResult(resource: Resource<T>)
    fun emitData(dataBlock: EmitDataBlock<T>)
    fun retry()
    suspend fun emit(data: T)
    suspend fun emitSource(source: LiveData<Resource<T>>): DisposableHandle
    fun autoRetry(bool: Boolean)
    fun cancelable(bool: Boolean)
    fun viewType(viewType: ViewType)
    fun retryable(bool: Boolean)
    fun errorMapper(errorMapper: ErrorMapper)
    fun resourceMapper(resourceMapper: ResourceMapper<*>)
    fun result(): Resource<T>?
    fun <R> onSuccess(action: (R) -> Unit)
    fun onError(action: (Exception) -> Unit)
    fun onLoading(action: (Any?) -> Unit)
    fun onRun(block: suspend () -> Unit)
}

fun interface EmitDataBlock<T> {
    fun invoke(): T
}

fun interface EmitResultBlock<T> {
    fun invoke(): Resource<T>
}
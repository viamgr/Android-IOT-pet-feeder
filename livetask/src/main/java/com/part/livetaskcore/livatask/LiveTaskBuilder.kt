package com.part.livetaskcore.livatask

import androidx.lifecycle.LiveData
import com.part.livetaskcore.ErrorMapper
import com.part.livetaskcore.Resource
import com.part.livetaskcore.bindingadapter.ViewType
import kotlinx.coroutines.DisposableHandle

/**
 * Interface that allows controlling a LiveTask from a coroutine block.
 */
interface LiveTaskBuilder<T> {
    suspend fun emit(result: Resource<T>)
    suspend fun emit(result: T)
    suspend fun emitSource(source: LiveData<Resource<T>>): DisposableHandle
    fun autoRetry(bool: Boolean)
    fun cancelable(bool: Boolean)
    fun viewType(viewType: ViewType)
    fun retryable(bool: Boolean)
    fun errorMapper(errorMapper: ErrorMapper)
    fun onSuccess(action: (T?) -> Unit)
    fun onError(action: (Exception) -> Unit)
    fun onLoading(action: (Any?) -> Unit)
}
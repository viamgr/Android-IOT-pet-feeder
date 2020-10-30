package com.viam.feeder.core.task

import com.viam.feeder.core.Resource
import kotlin.coroutines.CoroutineContext

interface RequestBuilder<R, T> {
    fun withContext(
        coroutineContext: CoroutineContext,
        block: suspend (R) -> Unit
    )

    fun initialParams(params: R)

    fun emit(resultValue: Resource<T>)
}
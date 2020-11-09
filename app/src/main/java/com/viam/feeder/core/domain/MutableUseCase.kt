package com.viam.feeder.core.domain

import androidx.lifecycle.MutableLiveData
import kotlinx.coroutines.CoroutineDispatcher

/**
 * Executes business logic synchronously or asynchronously using Coroutines.
 */
abstract class MutableUseCase<in P, R>(private val coroutineDispatcher: CoroutineDispatcher) {

    /** Executes the use case asynchronously and returns a [Result].
     *
     * @return a [Result].
     *
     * @param parameters the input parameters to run the use case with
     */
    suspend operator fun invoke(parameters: P): MutableLiveData<R> {
        return execute(parameters)
    }

    /**
     * Override this to set the code to be executed.
     */
    @Throws(RuntimeException::class)
    protected abstract suspend fun execute(parameters: P): MutableLiveData<R>
}
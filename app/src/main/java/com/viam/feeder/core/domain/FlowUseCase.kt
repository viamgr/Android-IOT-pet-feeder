package com.viam.feeder.core.domain

import com.part.livetaskcore.usecases.ParameterFlow
import com.viam.resource.Resource
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flowOn

/**
 * Executes business logic in its execute method and keep posting updates to the Resource as
 * [Resource<R>].
 * Handling an exception (emit [Resource.Error] to the Resource) is the subclasses's responsibility.
 */
abstract class FlowUseCase<in P, R>(private val coroutineDispatcher: CoroutineDispatcher) :
    ParameterFlow<P, R> {

    protected abstract fun execute(parameter: P): Flow<Resource<R>>
    override operator fun invoke(parameter: P): Flow<Resource<R>> = execute(parameter)
        .catch { e ->
            e.printStackTrace()
            emit(Resource.Error(Exception(e)))
        }
        .flowOn(coroutineDispatcher)
}
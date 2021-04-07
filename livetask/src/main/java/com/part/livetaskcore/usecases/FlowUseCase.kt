package com.part.livetaskcore.usecases

import com.viam.resource.Resource
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flowOn

abstract class FlowUseCase<in P, R>(private val coroutineDispatcher: CoroutineDispatcher) :
    ParameterFlow<P, R> {
    abstract suspend fun execute(params: P): Flow<Resource<R>>

    override suspend operator fun invoke(parameters: P): Flow<Resource<R>> {
        return execute(parameters)
            .flowOn(coroutineDispatcher)
            .catch { e ->
                emit(Resource.Error(Exception(e)))
            }
    }
}
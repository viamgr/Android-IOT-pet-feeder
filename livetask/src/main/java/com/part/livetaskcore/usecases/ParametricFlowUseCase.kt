package com.part.livetaskcore.usecases

import kotlinx.coroutines.flow.Flow

interface ParametricFlowUseCase<in P, R> {
    operator fun invoke(parameter: P): Flow<R>
}

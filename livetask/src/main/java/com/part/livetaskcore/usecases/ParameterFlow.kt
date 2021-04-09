package com.part.livetaskcore.usecases

import com.viam.resource.Resource
import kotlinx.coroutines.flow.Flow

interface ParameterFlow<in P, R> {
    operator fun invoke(parameter: P): Flow<Resource<R>>
}

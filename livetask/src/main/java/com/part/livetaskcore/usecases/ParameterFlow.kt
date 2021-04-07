package com.part.livetaskcore.usecases

import com.viam.resource.Resource
import kotlinx.coroutines.flow.Flow

interface ParameterFlow<in P, R> {
    suspend operator fun invoke(parameters: P): Flow<Resource<R>>
}

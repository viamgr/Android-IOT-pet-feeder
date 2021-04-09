package com.part.livetaskcore.usecases

import com.viam.resource.Resource

interface ParameterResource<in P, R> {
    suspend operator fun invoke(parameters: P): Resource<R>
}

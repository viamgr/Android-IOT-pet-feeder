package com.part.livetaskcore.usecases

interface ParametricResource<in P, R> {
    suspend operator fun invoke(parameter: P): R
}

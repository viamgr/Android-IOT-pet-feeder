package com.part.livetaskcore.usecases

interface ParametricUseCase<in P, R> {
    suspend operator fun invoke(parameter: P): R
}

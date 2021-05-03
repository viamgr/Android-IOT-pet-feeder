package com.part.livetaskcore.livatask

interface ParametricLiveTaskBuilder<P, T> : LiveTaskBuilder<T> {
    fun withParameter(parameter: P)
}

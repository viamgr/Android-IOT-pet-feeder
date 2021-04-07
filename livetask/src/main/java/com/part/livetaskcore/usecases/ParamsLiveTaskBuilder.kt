package com.part.livetaskcore.usecases

import com.part.livetaskcore.livatask.LiveTaskBuilder

interface ParamsLiveTaskBuilder<in P, T> : LiveTaskBuilder<T> {
    fun params(block: () -> P)
}

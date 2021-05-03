package com.part.livetaskcore.livatask

import com.part.livetaskcore.LiveTaskManager

class ParametricCoroutineLiveTask<P, T>(
    parametricBlock: suspend ParametricLiveTaskBuilder<P, T>.() -> Unit = {},
    liveTaskManager: LiveTaskManager = LiveTaskManager.instance,
) : CoroutineLiveTask<T>(liveTaskManager = liveTaskManager), ParametricLiveTask<P, T>,
    ParametricLiveTaskBuilder<P, T> {

    override val block = parametricBlock as suspend LiveTaskBuilder<T>.() -> Unit

    private var parameter: P? = null
    override fun getParameter(): P {
        return parameter!!
    }

    override fun setParameter(parameter: P): ParametricLiveTask<P, T> {
        this.parameter = parameter
        return this
    }

    private suspend fun run(parameter: P): ParametricLiveTask<P, T> {
        setParameter(parameter)
        run()
        return this
    }

    override suspend fun invoke(parameter: P): ParametricLiveTask<P, T> {
        return run(parameter)
    }

    override fun withParameter(parameter: P) {
        setParameter(parameter)
    }
}
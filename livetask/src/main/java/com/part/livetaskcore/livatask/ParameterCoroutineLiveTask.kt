package com.part.livetaskcore.livatask

class ParameterCoroutineLiveTask<P, T>(
    override val block: suspend LiveTaskBuilder<T>.() -> Unit = {}
) : CoroutineLiveTask<T>(), ParameterLiveTask<P, T> {
    var param: P? = null
    override fun getParameter(): P {
        return param!!
    }

    override fun setParameter(parameter: P): LiveTask<T> {
        param = parameter
        return this
    }

    override suspend fun run(parameter: P): LiveTask<T> {
        setParameter(parameter)
        run()
        return this
    }

}
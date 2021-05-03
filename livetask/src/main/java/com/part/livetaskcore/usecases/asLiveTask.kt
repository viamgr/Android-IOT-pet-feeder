package com.part.livetaskcore.usecases

import com.part.livetaskcore.Resource
import com.part.livetaskcore.livatask.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlin.experimental.ExperimentalTypeInference

@OptIn(ExperimentalTypeInference::class)
fun <T> Flow<Resource<T>>.asLiveTask(
    @BuilderInference block: suspend LiveTaskBuilder<T>.() -> Unit = {},
): LiveTask<T> {
    return liveTask {
        val liveTaskBuilder = this
        block.invoke(liveTaskBuilder)
        collect {
            liveTaskBuilder.emit(it)
        }
    }
}

inline fun <P, R> ParametricFlow<P, R>.asLiveTask(
    crossinline builder: (ParametricLiveTaskBuilder<P, R>.() -> Unit) = {},
): ParametricLiveTask<P, R> {
    var liveTask: ParametricLiveTask<P, R>? = null
    liveTask = parametricLiveTask {
        builder.invoke(this)
        invoke(liveTask!!.getParameter())
            .collect {
                emit(it)
            }
    }
    return liveTask
}

inline fun <P, R> ParametricResource<P, R>.asLiveTask(
    crossinline builder: (ParametricLiveTaskBuilder<P, R>.() -> Unit) = {},
): ParametricLiveTask<P, R> {
    var liveTask: ParametricLiveTask<P, R>? = null
    liveTask = parametricLiveTask {
        builder.invoke(this)
        val input = invoke(liveTask!!.getParameter())
        emit(input)
    }
    return liveTask
}

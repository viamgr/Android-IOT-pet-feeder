package com.part.livetaskcore.usecases

import com.part.livetaskcore.livatask.*
import com.viam.resource.Resource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlin.experimental.ExperimentalTypeInference

@OptIn(ExperimentalTypeInference::class)
fun <T> Flow<Resource<T>>.asLiveTask(
    @BuilderInference block: suspend LiveTaskBuilder<T>.() -> Unit = {},
): LiveTask<T> {
    return liveTask {
        block.invoke(this)
        collect {
            emit(it)
        }
    }
}

inline fun <P, R> ParameterFlow<P, R>.asLiveTask(
    crossinline builder: (LiveTaskBuilder<R>.() -> Unit) = {}
): ParameterLiveTask<P, R> {
    var liveTask: ParameterLiveTask<P, R>? = null
    liveTask = parameterLiveTask {
        builder.invoke(this)
        invoke(liveTask!!.getParameter())
            .collect {
                emit(it)
            }
    }
    return liveTask
}

package com.part.livetaskcore.usecases

import com.part.livetaskcore.LiveTaskManager
import com.part.livetaskcore.livatask.ParametricLiveTask
import com.part.livetaskcore.livatask.ParametricLiveTaskBuilder
import com.part.livetaskcore.livatask.parametricLiveTask
import kotlinx.coroutines.flow.collect

/*
@OptIn(ExperimentalTypeInference::class)
fun <T> Flow<Resource<T>>.asLiveTask(
    @BuilderInference block: LiveTaskBuilder<T>.() -> Unit = {},
): LiveTask<T> {
    return liveTask {
        val liveTaskBuilder = this
        block.invoke(liveTaskBuilder)
        collect {
            liveTaskBuilder.emit {
                it
            }
        }
    }
}*/

inline fun <P, R> ParametricFlowUseCase<P, R>.asLiveTask(
    liveTaskManager: LiveTaskManager = LiveTaskManager.instance,
    crossinline builder: (ParametricLiveTaskBuilder<P, R>.() -> Unit) = {},
): ParametricLiveTask<P, R> {
    var parametricLiveTask: ParametricLiveTask<P, R>? = null
    parametricLiveTask = parametricLiveTask(liveTaskManager) {
        builder()
        onRun {
            val flowData = invoke(parametricLiveTask!!.getParameter())
            flowData.collect {
                emit(it)
            }
        }

    }
    return parametricLiveTask
}

inline fun <P, R> ParametricUseCase<P, R>.asLiveTask(
    crossinline builder: (ParametricLiveTaskBuilder<P, R>.() -> Unit) = {},
): ParametricLiveTask<P, R> {
    var liveTask: ParametricLiveTask<P, R>? = null
    liveTask = parametricLiveTask {
        builder()
        onRun {
            val input = invoke(liveTask!!.getParameter())
            emitData { input }
        }
    }
    return liveTask
}

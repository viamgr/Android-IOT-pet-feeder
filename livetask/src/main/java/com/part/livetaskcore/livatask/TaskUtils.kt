package com.part.livetaskcore.livatask

import com.part.livetaskcore.LiveTaskManager
import kotlin.experimental.ExperimentalTypeInference

/**
 * Builds a Livetask that has values yielded from the given [block] that executes on a
 * [LiveTaskBuilder].
 */
@OptIn(ExperimentalTypeInference::class)
fun <T> liveTask(
    @BuilderInference block: suspend LiveTaskBuilder<T>.() -> Unit = {}
): LiveTask<T> = CoroutineLiveTask(
    block = block
)

/**
 * Builds a TaskCombiner object that has values yielded from the given [LiveTask]s that executes on a
 * [CombinerBuilder].
 */
@OptIn(ExperimentalTypeInference::class)
fun combine(
    vararg requests: LiveTask<*>,
    @BuilderInference block: LiveTaskBuilder<Any>.() -> Unit = {}
): LiveTask<Any> {
    return TaskCombiner(*requests, block = block)
}


@OptIn(ExperimentalTypeInference::class)
fun <P, T> parametricLiveTask(
    liveTaskManager: LiveTaskManager = LiveTaskManager.instance,
    @BuilderInference block: suspend ParametricLiveTaskBuilder<P, T>.() -> Unit = {},
): ParametricLiveTask<P, T> = ParametricCoroutineLiveTask(
    parametricBlock = block, liveTaskManager
)
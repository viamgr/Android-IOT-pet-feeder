package com.part.livetaskcore.livatask

import com.part.livetaskcore.LiveTaskManager
import kotlin.experimental.ExperimentalTypeInference

/**
 * Builds a Livetask that has values yielded from the given [block] that executes on a
 * [LiveTaskBuilder].
 */
@OptIn(ExperimentalTypeInference::class)
fun <T> liveTask(
    @BuilderInference block: LiveTaskBuilder<T>.() -> Unit = {}
): LiveTask<T> = CoroutineLiveTask(
    block = block
)

/**
 * Builds a TaskCombiner object that has values yielded from the given [LiveTask]s that executes on a
 * [CombinedLiveTask].
 */
@OptIn(ExperimentalTypeInference::class)
fun combine(
    vararg requests: LiveTask<*>,
    @BuilderInference block: LiveTaskBuilder<Any>.() -> Unit = {}
): LiveTask<Any> {
    return CombinedLiveTask(*requests, block = block)
}


@OptIn(ExperimentalTypeInference::class)
fun <P, T> parametricLiveTask(
    liveTaskManager: LiveTaskManager = LiveTaskManager.instance,
    @BuilderInference block: ParametricLiveTaskBuilder<P, T>.() -> Unit = {},
): ParametricLiveTask<P, T> = ParametricCoroutineLiveTask(
    liveTaskManager,
    parametricBlock = block
)
package com.part.livetaskcore.livatask

import com.part.livetaskcore.Resource
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

internal typealias Block<T> = suspend LiveTaskBuilder<T>.() -> Unit

/**
 * Handles running a block at most once to completion.
 */
internal const val DEFAULT_TIMEOUT = 250L

class TaskRunner<T>(
    private val liveData: LiveTaskBuilder<T>,
    private val block: Block<T>,
    private val timeoutInMs: Long = DEFAULT_TIMEOUT,
    private val scope: CoroutineScope,
    private val onDone: () -> Unit
) {
    private var runningJob: Job? = null

    private var cancellationJob: Job? = null

    fun maybeRun() {
        cancellationJob?.cancel()
        cancellationJob = null
        if (runningJob != null) {
            return
        }
        runningJob = scope.launch {
            block(liveData)
            onDone()
        }
    }

    fun cancel(immediately: Boolean) {
        cancellationJob = scope.launch(Dispatchers.Main.immediate) {
            if (!immediately)
                delay(timeoutInMs)
            runningJob?.cancel()
            runningJob = null
            liveData.emitResult(Resource.Error(CancellationException()))
        }
    }
}


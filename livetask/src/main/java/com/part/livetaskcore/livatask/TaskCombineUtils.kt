package com.part.livetaskcore.livatask

import com.part.livetaskcore.ErrorMapper
import com.part.livetaskcore.ErrorObserverCallback
import com.part.livetaskcore.bindingadapter.ProgressType
import com.viam.resource.Resource
import kotlinx.coroutines.*

/**
 * Handles running a block at most once to completion.
 */
internal class CombineRunner(
    private val liveData: TaskCombiner,
    private val block: CombinerBlock,
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
            val liveDataScope = CombinerBuilderImpl(liveData)
            block(liveDataScope)
            onDone()
        }
    }

    fun cancel() {
        cancellationJob = scope.launch(Dispatchers.Main.immediate) {
            delay(timeoutInMs)
            runningJob?.cancel()
            runningJob = null
        }
    }
}

internal class CombinerBuilderImpl(
    private var target: TaskCombiner,
) : CombinerBuilder {

    override val latestValue: Resource<Any>?
        get() = target.value?.result()

    override fun cancelable(bool: Boolean) {
        target.cancelable(bool)
    }

    override fun loadingViewType(type: ProgressType) {
        target.loadingViewType = type
    }

    override fun retryable(bool: Boolean) {
        target.retryable(bool)
    }

    override fun retryAttempts(attempts: Int) {
        target.setRetryAttemptsTasks(attempts)
    }

    override fun autoRetry(bool: Boolean) {
        target.setAutoRetryForAll(bool)
    }

    override fun errorMapper(errorMapper: ErrorMapper) {
        target.setErrorMapper(errorMapper)
    }

    override fun errorObserver(errorObserver: ErrorObserverCallback) {
        target.setErrorObserver(errorObserver)
    }

    override fun onSuccess(action: (Any?) -> Unit) {
        target.setSuccessAction(action)
    }

    override fun onError(action: (Exception) -> Unit) {
        target.setErrorAction(action)
    }

    override fun onLoading(action: (Any?) -> Unit) {
        target.setLoadingAction(action)
    }
}

internal typealias CombinerBlock = suspend CombinerBuilder.() -> Unit
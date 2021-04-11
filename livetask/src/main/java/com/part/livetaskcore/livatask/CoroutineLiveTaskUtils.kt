package com.part.livetaskcore.livatask

import androidx.annotation.MainThread
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import com.part.livetaskcore.ErrorMapper
import com.part.livetaskcore.ErrorObserverCallback
import com.part.livetaskcore.bindingadapter.ProgressType
import com.viam.resource.Resource
import kotlinx.coroutines.*
import kotlin.coroutines.CoroutineContext

/**
 * Handles running a block at most once to completion.
 */
internal class TaskRunner<T>(
    private val liveData: CoroutineLiveTask<T>,
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
            val liveDataScope = LiveTaskBuilderImpl(liveData, coroutineContext)
            block(liveDataScope)
            onDone()
        }
    }

    fun cancel() {
        cancellationJob = scope.launch(Dispatchers.Main.immediate) {
            delay(timeoutInMs)
            runningJob?.cancel()
            runningJob = null
            liveData.handleResult(Resource.Error(CancellationException()))
        }
    }
}

internal class LiveTaskBuilderImpl<T>(
    private var target: CoroutineLiveTask<T>,
    context: CoroutineContext
) : LiveTaskBuilder<T> {

    override val latestValue: Resource<T>?
        get() = target.value?.result()

    private val coroutineContext = context + Dispatchers.Main.immediate

    override suspend fun emit(result: Resource<T>) {
        target.clearSource()
        withContext(coroutineContext) {
            println("emit $result")
            target.handleResult(result)
        }
    }

    override suspend fun emitSource(source: LiveData<Resource<T>>): DisposableHandle =
        withContext(coroutineContext) {
            return@withContext target.emitSource(source)
        }

    override fun autoRetryAttempts(attempts: Int) {
        target.retryAttempts = attempts
    }

    override fun retryOnNetworkEstablishment(bool: Boolean) {
        target.autoRetry = bool
    }

    override fun cancelable(bool: Boolean) {
        target.cancelable(bool)
    }

    override fun retryable(bool: Boolean) {
        target.retryable(bool)
    }

    override fun loadingViewType(type: ProgressType) {
        target.loadingViewType = type
    }

    override fun errorMapper(errorMapper: ErrorMapper) {
        target.setErrorMapper(errorMapper)
    }

    override fun errorObserver(errorObserver: ErrorObserverCallback) {
        target.setErrorObserver(errorObserver)
    }

    override fun onSuccess(action: (T?) -> Unit) {
        target.setSuccessAction(action)
    }

    override fun onError(action: (Exception) -> Unit) {
        target.setErrorAction(action)
    }

    override fun onLoading(action: (Any?) -> Unit) {
        target.setLoadingAction(action)
    }
}

/**
 * Holder class that keeps track of the previously dispatched LiveTask.
 * It implements [DisposableHandle] interface while also providing a suspend clear function
 * that we can use internally.
 */
internal class Emitted(
    private val source: LiveData<*>,
    private val mediator: MediatorLiveData<*>
) : DisposableHandle {
    // @MainThread
    private var disposed = false

    /**
     * Unlike [dispose] which cannot be sync because it not a coroutine (and we do not want to
     * lock), this version is a suspend function and does not return until source is removed.
     */
    suspend fun disposeNow() = withContext(Dispatchers.Main.immediate) {
        removeSource()
    }

    override fun dispose() {
        CoroutineScope(Dispatchers.Main.immediate).launch {
            removeSource()
        }
    }

    @MainThread
    private fun removeSource() {
        if (!disposed) {
            mediator.removeSource(source)
            disposed = true
        }
    }
}

internal typealias Block<T> = suspend LiveTaskBuilder<T>.() -> Unit
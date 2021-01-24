@file:JvmName("LiveTaskKt")

package com.viam.feeder.core.task

import androidx.annotation.MainThread
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import com.viam.feeder.core.Resource
import com.viam.feeder.core.domain.utils.isConnectionError
import com.viam.feeder.core.onSuccess
import kotlinx.coroutines.*
import java.util.concurrent.CancellationException
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

internal typealias Block<P, R> = suspend LiveTaskScope<P, R>.(params: P?) -> Unit

class CoroutineLiveTask<P, R>(
    context: CoroutineContext = EmptyCoroutineContext,
    private val requestBlock: Block<P, R>,
    private val logger: TaskEventLogger = TaskEventLogger,
    private val autoRetryHandler: LiveData<Boolean> = AutoRetryHandler,
) : MediatorLiveData<LiveTask<P, R>>(), LiveTask<P, R>,
    LiveTaskScope<P, R> {
    private var successBlock: ((resource: R?) -> Unit)? = null
    private var runningJob: Job? = null
    private var _state: Resource<R>? = null
    private var params: P? = null
    private var cancelable: Boolean = true

    private var retryAfterActive: Boolean? = null
    private var scope: CoroutineScope

    init {
        // use an intermediate supervisor job so that if we cancel individual block runs due to losing
        // observers, it won't cancel the given context as we only cancel w/ the intention of possibly
        // relaunching using the same parent context.
        val supervisorJob = SupervisorJob(context[Job])

        // The scope for this LiveData where we launch every block Job.
        // We default to Main dispatcher but developer can override it.
        // The supervisor job is added last to isolate block runs.
        scope = CoroutineScope(Dispatchers.Main.immediate + context + supervisorJob)
    }

    @MainThread
    fun maybeRun() {
        runningJob = scope.launch {
            val notifyImmediately = _state is Resource.Error
            val loadingJob = if (notifyImmediately) {
                _state = Resource.Loading
                notifyValue()
                null
            } else {
                _state = null
                launch {
                    if (isActive) {
                        _state = Resource.Loading
                        notifyValue()
                    }
                }
            }

            requestBlock(this@CoroutineLiveTask, params)
            notifyValue()
            loadingJob?.cancel()
        }
    }

    override fun onInactive() {
        super.onInactive()
        removeSource(autoRetryHandler)
        if (runningJob?.isCompleted == false) {
            retryAfterActive = true
        }
        runningJob?.cancel()
        runningJob = null
    }

    override fun onActive() {
        super.onActive()
        if (retryAfterActive == true) {
            retryAfterActive = false
            maybeRun()
        }
        handleAutoRetry()
    }

    private fun handleAutoRetry() {
        addSource(autoRetryHandler) { isAvailableConnection ->
            if (isAvailableConnection && _state is Resource.Error &&
                (_state as Resource.Error).exception.isConnectionError()
            ) {
                retry()
            }
        }
    }

    @MainThread
    override fun cancel() {
        if (!cancelable) {
            return
        }
        runningJob?.cancel()
        runningJob = null
        _state = null
        notifyValue()
    }

    override suspend fun emit(resource: Resource<R>?) {
        _state = if (resource is Resource.Error && resource.exception is CancellationException) {
            null
        } else {
            resource
        }
        notifyValue()
    }

    override fun retry() {
        if (_state is Resource.Error && (_state as Resource.Error).exception !is CancellationException) {
            cancel()
            maybeRun()
        }
    }

    override fun execute(params: P?): LiveTask<P, R> {
        this.params = params
        maybeRun()
        return this
    }

    override fun state() = _state

    override fun asLiveData(): MediatorLiveData<LiveTask<P, R>> = this

    private fun notifyValue() {
        value = this
        _state?.onSuccess {
            successBlock?.invoke(it)
        }
        logger.newEvent(_state)
    }

    override fun onSuccess(block: (resource: R?) -> Unit): LiveTask<P, R> {
        successBlock = block
        return this
    }

    override fun initialParams(params: P) {
        this.params = params
    }

    override fun postWithCancel(params: P?) {
        cancel()
        execute(params ?: this.params)
    }

    override fun cancelable(cancelable: Boolean) {
        this.cancelable = cancelable
    }

    override fun isCancelable() = this.cancelable
    override fun params(): P? = params
}


fun <P, R> livaTask(
    context: CoroutineContext = EmptyCoroutineContext,
    logger: TaskEventLogger = TaskEventLogger,
    autoRetryHandler: LiveData<Boolean> = AutoRetryHandler,
    requestBlock: Block<P, R>
): LiveTask<P, R> {
    return CoroutineLiveTask(
        context,
        requestBlock = requestBlock,
        logger = logger,
        autoRetryHandler = autoRetryHandler
    )
}


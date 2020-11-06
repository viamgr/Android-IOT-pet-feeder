@file:JvmName("LiveTaskKt")

package com.viam.feeder.core.task

import androidx.annotation.MainThread
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import com.viam.feeder.core.Resource
import com.viam.feeder.core.domain.isConnectionError
import kotlinx.coroutines.*
import java.util.concurrent.CancellationException
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

internal typealias Block<P, R> = suspend PromiseTaskScope<P, R>.(params: P) -> Unit

class CoroutineLiveTask<P, R>(
    context: CoroutineContext = EmptyCoroutineContext,
    private val requestBlock: Block<P, R>,
    private val logger: TaskEventLogger = TaskEventLogger,
    private val autoRetryHandler: LiveData<Boolean> = AutoRetryHandler,
) : MediatorLiveData<LiveTask<P, R>>(), LiveTask<P, R>,
    PromiseTaskScope<P, R> {
    private var runningJob: Job? = null
    private var _state: Resource<R>? = null
    private var params: P? = null

    // use `liveData` provided context + main dispatcher to communicate with the target
    // LiveData. This gives us main thread safety as well as cancellation cooperation
    private var context: CoroutineContext? = null
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
        if (runningJob != null) {
            return
        }
        runningJob = scope.launch {
            context = coroutineContext + Dispatchers.Main.immediate
            _state = Resource.Loading
            notifyValue()
            requestBlock(this@CoroutineLiveTask, params!!)
            runningJob?.cancel()
            runningJob = null
            context = null
        }
    }

    override fun onInactive() {
        super.onInactive()
        removeSource(autoRetryHandler)
        cancel(false)
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
        cancel(true)
    }

    override suspend fun emit(resource: Resource<R>?) {
        context?.let {
            withContext(it) {
                _state = resource
                notifyValue()
            }
        }
    }

    override fun retry() {
        if (params != null && _state is Resource.Error && (_state as Resource.Error).exception !is CancellationException)
            maybeRun()
    }

    override fun execute(params: P) {
        this.params = params
        maybeRun()
    }

    override fun state() = _state


    override fun asLiveData(): LiveData<LiveTask<P, R>>? = this

    private fun notifyValue() {
        value = this
        logger.newEvent(_state)
    }

    private fun cancel(userCanceled: Boolean) {
        if (runningJob?.isCompleted == false) {
            retryAfterActive = true
        }

        if (runningJob?.isActive == true)
            _state = Resource.Error(CancellationException())
        else if (userCanceled) {
            _state = null
        }
        notifyValue()

        runningJob?.cancel()
        runningJob = null
        context = null
    }

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


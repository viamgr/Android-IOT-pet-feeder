package com.viam.feeder.core.task

import androidx.annotation.MainThread
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import com.viam.feeder.core.Resource
import kotlinx.coroutines.*
import java.util.concurrent.CancellationException
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

internal typealias Block<R, T> = suspend PromiseTaskScope<R, T>.(params: R) -> Unit

class CoroutineLiveTask<R, T>(
    context: CoroutineContext = EmptyCoroutineContext,
    private val requestBlock: Block<R, T>,
) : MediatorLiveData<LiveTask<R, T>>(), LiveTask<R, T>,
    PromiseTaskScope<R, T> {
    private val logger: EventLogger = TaskEventLogger

    // currently running block job.
    private var runningJob: Job? = null
    private var _state: Resource<T>? = null
    private var params: R? = null

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

    override fun onActive() {
        super.onActive()
        if (retryAfterActive == true) {
            retryAfterActive = false
            maybeRun()
        }
    }

    @MainThread
    override fun cancel() {
        cancel(true)
    }

    override suspend fun emit(resource: Resource<T>?) {
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

    override fun execute(params: R) {
        this.params = params
        maybeRun()
    }

    override fun state() = _state

    override fun onInactive() {
        super.onInactive()
        cancel(false)
    }

    override fun asLiveData(): LiveData<LiveTask<R, T>>? = this

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


fun <R, T : Any> livaTask(
    context: CoroutineContext = EmptyCoroutineContext,
    requestBlock: Block<R, T>
): LiveTask<R, T> {
    return CoroutineLiveTask(context, requestBlock)
}
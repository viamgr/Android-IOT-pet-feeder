package com.part.livetaskcore.livatask

import androidx.annotation.CallSuper
import androidx.annotation.VisibleForTesting
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import com.part.livetaskcore.ErrorMapper
import com.part.livetaskcore.LiveTaskManager
import com.part.livetaskcore.Resource
import com.part.livetaskcore.ResourceMapper
import com.part.livetaskcore.views.ViewType
import kotlin.coroutines.cancellation.CancellationException

/**
 * Contains common functions and properties of a LiveTask.
 * */
//TODO find a way to remove live task from this class
abstract class BaseLiveTask<T>(liveTaskManager: LiveTaskManager) : MediatorLiveData<LiveTask<T>>(),
    LiveTask<T>, LiveTaskBuilder<T> {
    protected open val mediatorLiveResult = MediatorLiveData<Resource<T>?>()
    override val liveResult: LiveData<Resource<T>?>
        get() = mediatorLiveResult
    protected var cancelable: Boolean? = null
    protected var viewType: ViewType? = null
    protected var autoRetry: Boolean? = false
    protected var retryable: Boolean? = null
    protected var blockRunner: TaskRunner<T>? = null
    protected var onSuccessAction: (Any?) -> Unit = {}
    protected var onRunCallback: (suspend () -> Unit)? = null
    protected var onErrorAction: (Exception) -> Unit = {}
    protected var errorMapper: ErrorMapper = liveTaskManager.errorMapper
    protected var resourceMapper: ResourceMapper<*>? = liveTaskManager.resourceMapper
    protected var onLoadingAction: (Any?) -> Unit = {}

    @VisibleForTesting
    fun isRetryableForTesting() = retryable

    private var latestResult: Resource<T>? = null
        set(value) {
            when {
                value is Resource.Success -> {
                    fireChanges(value)
                    field = value
                    onSuccessHappened(value)
                }
                value is Resource.Error && value.exception !is CancellationException
                -> {
                    val error = mapError(value)
                    fireChanges(error)
                    field = value
                    onErrorHappened(value)
                }
                value is Resource.Loading -> {
                    fireChanges(value)
                    field = value
                    onLoadingHappened(value)
                }
                else -> {
                    field = value
                    fireChanges(value)
                }
            }
        }

    protected fun setResult(result: Resource<T>?) {
        latestResult = result
    }

    @VisibleForTesting
    public fun setResultForTest(result: Resource<T>?) {
        latestResult = result
    }

    @CallSuper
    protected open fun onLoadingHappened(value: Resource.Loading) {
        onLoadingAction(value.data)
    }

    @CallSuper
    @Suppress("UNCHECKED_CAST")
    protected open fun onSuccessHappened(value: Resource.Success<*>) {
        onSuccessAction(value.data)
    }

    @CallSuper
    @Suppress("UNCHECKED_CAST")
    protected open fun onErrorHappened(value: Resource.Error) {
        onErrorAction(value.exception)
    }

    private fun fireChanges(resource: Resource<T>?) {
        postValue(this)
        mediatorLiveResult.postValue(resource)
    }

    private fun mapError(value: Resource.Error): Resource.Error {
        val mapError = errorMapper.mapError(value.exception)
        return mapError.let { Resource.Error(it) }
    }

    override fun loadingViewType(): ViewType? = viewType
    override fun asLiveData(): LiveData<LiveTask<T>> = this
    override fun result(): Resource<T>? = latestResult

    override fun cancelable(bool: Boolean) {
        cancelable = bool
    }

    override fun viewType(viewType: ViewType) {
        this.viewType = viewType
    }

    override fun autoRetry(bool: Boolean) {
        autoRetry = bool
    }

    override fun retryable(bool: Boolean) {
        retryable = bool
    }

    override fun resourceMapper(resourceMapper: ResourceMapper<*>) {
        this.resourceMapper = resourceMapper
    }

    override fun errorMapper(errorMapper: ErrorMapper) {
        this.errorMapper = errorMapper
    }

    override fun <R> onSuccess(action: (R) -> Unit) {
        @Suppress("UNCHECKED_CAST")
        onSuccessAction = action as (Any?) -> Unit
    }

    override fun onError(action: (Exception) -> Unit) {
        onErrorAction = action
    }

    override fun onLoading(action: (Any?) -> Unit) {
        onLoadingAction = action
    }

    override fun onRun(block: suspend () -> Unit) {
        onRunCallback = block
    }

    @CallSuper
    protected open fun applyResult(result: Resource<T>) {
        latestResult = result
    }
}
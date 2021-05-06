package com.part.livetaskcore.livatask

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import com.part.livetaskcore.ErrorMapper
import com.part.livetaskcore.LiveTaskManager
import com.part.livetaskcore.Resource
import com.part.livetaskcore.views.CircularViewType
import com.part.livetaskcore.views.ViewType
import com.part.livetaskcore.withResult
import kotlin.coroutines.cancellation.CancellationException


/**
 * Contains common functions and properties of a LiveTask.
 * */
abstract class BaseLiveTask<T>(liveTaskManager: LiveTaskManager) : MediatorLiveData<LiveTask<T>>(),
    LiveTask<T>, LiveTaskBuilder<T> {

    protected var cancelable: Boolean? = true
    protected var autoRetry: Boolean? = false
    protected var retryable: Boolean? = true
    protected var blockRunner: TaskRunner<T>? = null


    protected var onSuccessAction: (T?) -> Unit = {}
    protected var onErrorAction: (Exception) -> Unit = {}
    protected var errorMapper: ErrorMapper? = liveTaskManager.errorMapper

    protected var onLoadingAction: (Any?) -> Unit = {}
    var latestState: Resource<T>? = null

    override var loadingViewType: ViewType = CircularViewType()
    override fun asLiveData(): LiveData<LiveTask<T>> = this
    override fun result(): Resource<T>? = latestState

    fun hasError(liveTask: LiveTask<*>) =
        liveTask.result() is Resource.Error && (liveTask.result() as Resource.Error).exception !is CancellationException

    protected fun applyResult(result: Resource<T>) {
        this.latestState = result
        result.withResult(
            onSuccess = { onSuccessAction(it) },
            onError = { onErrorAction(it) },
            onLoading = { onLoadingAction(it) }
        )
        postValue(this)
    }

    override fun cancelable(bool: Boolean) {
        cancelable = bool
    }


    override fun viewType(viewType: ViewType) {
        loadingViewType = viewType
    }


    override fun autoRetry(bool: Boolean) {
        autoRetry = bool
    }

    override fun retryable(bool: Boolean) {
        retryable = bool
    }

    override fun errorMapper(errorMapper: ErrorMapper) {
        this.errorMapper = errorMapper
    }

    override fun onSuccess(action: (T?) -> Unit) {
        onSuccessAction = action
    }

    override fun onError(action: (Exception) -> Unit) {
        onErrorAction = action
    }

    override fun onLoading(action: (Any?) -> Unit) {
        onLoadingAction = action
    }

}
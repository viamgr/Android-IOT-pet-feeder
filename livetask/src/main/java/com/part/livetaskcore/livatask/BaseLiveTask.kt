package com.part.livetaskcore.livatask

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import com.part.livetaskcore.ErrorMapper
import com.part.livetaskcore.ErrorMapperImpl
import com.part.livetaskcore.LiveTaskManager
import com.part.livetaskcore.bindingadapter.ProgressType
import com.viam.resource.Resource
import com.viam.resource.withResult


/**
 * Contains common functions and properties of a LiveTask.
 * */
abstract class BaseLiveTask<T>(liveTaskManager: LiveTaskManager) : MediatorLiveData<LiveTask<T>>(),
    LiveTask<T>, LiveTaskBuilder<T> {

    protected var cancelable: Boolean? = true
    protected var autoRetry: Boolean? = true
    protected var retryable: Boolean? = true
    protected var blockRunner: TaskRunner<T>? = null


    protected var onSuccessAction: (T?) -> Unit = {}
    protected var onErrorAction: (Exception) -> Unit = {}
    protected var errorMapper: ErrorMapper? = liveTaskManager.getErrorMapper()

    protected var onLoadingAction: (Any?) -> Unit = {}
    var latestState: Resource<T>? = null

    override var loadingViewType = ProgressType.CIRCULAR
    override fun asLiveData(): LiveData<LiveTask<T>> = this
    override fun result(): Resource<T>? = latestState

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
        cancelable = true
    }


    override fun loadingViewType(type: ProgressType) {
        loadingViewType = type
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
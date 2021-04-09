package com.part.livetaskcore.livatask

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import com.part.livetaskcore.bindingadapter.ProgressType
import com.viam.resource.Resource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

internal const val DEFAULT_RETRY_ATTEMPTS = 0

/**
 * Contains common functions and properties of a LiveTask.
 * */
abstract class BaseLiveTask<T> : MediatorLiveData<LiveTask<T>>(), LiveTask<T> {
    var retryCounts = 1
    var retryAttempts = DEFAULT_RETRY_ATTEMPTS
    var latestState: Resource<T>? = null
    var cancelable = true
    var retryable = true
    override var loadingViewType = ProgressType.CIRCULAR

    fun cancelable(bool: Boolean): BaseLiveTask<T> {
        cancelable = bool
        return this
    }

    fun retryable(bool: Boolean): BaseLiveTask<T> {
        retryable = bool
        return this
    }

    fun loadingViewType(type: ProgressType): BaseLiveTask<T> {
        loadingViewType = type
        return this
    }

    @Suppress("UNCHECKED_CAST")
    override fun asLiveData(): LiveData<LiveTask<T>> = this

    override fun result() = latestState

    internal suspend fun addDisposableEmit(
        source: LiveData<Resource<T>>,
    ): Emitted = withContext(Dispatchers.Main.immediate) {
        addSource(source) {
            latestState = it
            value = this@BaseLiveTask
        }
        Emitted(source = source, mediator = this@BaseLiveTask)
    }
}
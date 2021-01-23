package com.viam.feeder.core.task


import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import com.viam.feeder.core.Resource

internal typealias CompositeBlock = (CompositeTaskBuilder.() -> Unit)?

class CompositeTask(
    requestBlock: CompositeBlock,
    private vararg val requests: LiveTask<*, *>
) : LiveTask<Any, Any>, CompositeTaskBuilder, MediatorLiveData<LiveTask<*, *>>() {
    private var onSuccessBlock: ((resource: Any?) -> Unit)? = null
    private var state: Resource<Any>? = null
    private var cancelable: Boolean = true

    init {
        requestBlock?.invoke(this)
    }

    override fun onActive() {
        super.onActive()
        requests.map { request ->
            addSource(request as LiveData<*>) { liveTask ->
                if (liveTask is LiveTask<*, *>) {
                    val notSuccessList = requests
                        .filterNot { it.state() == null || it.state() is Resource.Success }
                        .map { it.state() }
                    if (notSuccessList.isEmpty()) {
                        state = Resource.Success(Unit)
                        onSuccessBlock?.invoke(state)
                        value = this
                    } else {
                        if (!notSuccessList.any { it is Resource.Error }) {
                            state = Resource.Loading
                            value = this
                        } else {
                            val errors = notSuccessList.filterIsInstance(Resource.Error::class.java)
                                .map { it.exception }
                            state = Resource.Error(CompositeException(errors))
                            value = this
                        }
                    }
                }
            }
        }
    }

    override fun onInactive() {
        super.onInactive()
        requests.map { request ->
            removeSource(request as LiveData<*>)
        }
    }

    override fun retry() {
        requests.map {
            it.retry()
        }
    }

    override fun cancel() {
        requests.map {
            it.cancel()
        }
    }

    override fun state() = state
    override fun execute(params: Any?): LiveTask<Any, Any> {
        requests.map {
            it.retry()
        }
        return this
    }

    @Suppress("UNCHECKED_CAST")
    override fun asLiveData() = this as MediatorLiveData<LiveTask<Any, Any>>

    override fun postWithCancel(params: Any?) {
        cancel()
        retry()
    }

    override fun cancelable(cancelable: Boolean) {
        this.cancelable = cancelable
    }

    override fun isCancelable() = cancelable
}

fun compositeTask(
    vararg requests: LiveTask<*, *>,
    requestBlock: CompositeBlock = null
): LiveTask<Any, Any> {
    return CompositeTask(requestBlock, *requests)
}
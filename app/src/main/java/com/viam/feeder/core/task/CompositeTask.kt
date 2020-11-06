package com.viam.feeder.core.task


import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import com.viam.feeder.core.Resource

internal typealias CompositeBlock = (CompositeTaskBuilder.() -> Unit)?

class CompositeRequest(
    requestBlock: CompositeBlock,
    private vararg val requests: LiveTask<*, *>
) : LiveTask<Any, Any>, CompositeTaskBuilder, MediatorLiveData<LiveTask<*, *>>() {
    private var state: Resource<Any>? = null

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
    override fun execute(params: Any) {
        requests.map {
            it.retry()
        }
    }

    @Suppress("UNCHECKED_CAST")
    override fun asLiveData(): LiveData<LiveTask<Any, Any>>? = this as LiveData<LiveTask<Any, Any>>
}

fun compositeTask(
    vararg requests: LiveTask<*, *>,
    requestBlock: CompositeBlock = null
): LiveTask<Any, Any> {
    return CompositeRequest(requestBlock, *requests)
}
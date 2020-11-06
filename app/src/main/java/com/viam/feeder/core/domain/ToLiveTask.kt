package com.viam.feeder.core.domain

import com.viam.feeder.core.Resource
import com.viam.feeder.core.task.LiveTask
import com.viam.feeder.core.task.livaTask

fun <P, R> UseCase<P, R>.toLiveTask(onDone: ((Resource<R>) -> Unit)? = null): LiveTask<P, R> {
    return livaTask { params ->
        val resource = this@toLiveTask.invoke(params)
        emit(resource)
        onDone?.invoke(resource)
    }
}
package com.viam.feeder.core.domain

import com.viam.feeder.core.task.Block
import com.viam.feeder.core.task.LiveTask
import com.viam.feeder.core.task.livaTask

fun <P, R> UseCase<P, R>.toLiveTask(
    requestBlock: Block<P, R>? = null
): LiveTask<P, R> {
    return livaTask { params ->
        requestBlock?.invoke(this, params)
        val resource = this@toLiveTask.invoke(params)
        emit(resource)
    }
}
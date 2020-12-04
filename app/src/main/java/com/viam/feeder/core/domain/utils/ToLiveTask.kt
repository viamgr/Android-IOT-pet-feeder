package com.viam.feeder.core.domain.utils

import com.viam.feeder.core.domain.UseCase
import com.viam.feeder.core.task.Block
import com.viam.feeder.core.task.LiveTask
import com.viam.feeder.core.task.livaTask

fun <P, R> UseCase<P, R>.toLiveTask(
    requestBlock: Block<P, R>? = null
): LiveTask<P, R> {
    return livaTask { params ->
        requestBlock?.invoke(this, params)
        val resource = this@toLiveTask.invoke(this.params()!!)
        emit(resource)
    }
}
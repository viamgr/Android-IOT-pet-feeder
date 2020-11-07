package com.viam.feeder.core.task

interface CompositeTaskBuilder {
    fun cancelable(cancelable: Boolean = true)
}
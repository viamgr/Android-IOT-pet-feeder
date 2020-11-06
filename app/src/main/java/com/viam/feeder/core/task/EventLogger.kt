package com.viam.feeder.core.task

interface EventLogger<T> {
    fun newEvent(event: T?)
}

package com.viam.feeder.core.task

import com.viam.feeder.core.Resource

interface EventLogger {
    fun newEvent(resource: Resource<*>?)
}

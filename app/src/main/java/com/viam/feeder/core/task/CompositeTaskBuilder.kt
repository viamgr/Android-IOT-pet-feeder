package com.viam.feeder.core.task

interface CompositeTaskBuilder {
    suspend fun logger(logger: EventLogger)
}
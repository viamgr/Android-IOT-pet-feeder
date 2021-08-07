package com.viam.feeder.domain.usecase.event

import com.viam.feeder.domain.base.CoroutinesDispatcherProvider
import com.viam.feeder.domain.base.UseCase
import com.viam.feeder.domain.repositories.system.EventRepository
import javax.inject.Inject


class SetStatus @Inject constructor(
    coroutinesDispatcherProvider: CoroutinesDispatcherProvider,
    private val eventRepository: EventRepository
) : UseCase<Status, Unit>(coroutinesDispatcherProvider.io) {
    override suspend fun execute(parameters: Status) =
        eventRepository.setStatus(parameters.key, parameters.value)
}

data class Status(val key: String, val value: String)
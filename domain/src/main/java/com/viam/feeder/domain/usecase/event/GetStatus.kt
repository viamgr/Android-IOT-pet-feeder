package com.viam.feeder.domain.usecase.event

import com.viam.feeder.domain.base.CoroutinesDispatcherProvider
import com.viam.feeder.domain.base.UseCase
import com.viam.feeder.domain.repositories.system.EventRepository
import javax.inject.Inject


class GetStatus @Inject constructor(
    coroutinesDispatcherProvider: CoroutinesDispatcherProvider,
    private val eventRepository: EventRepository
) : UseCase<String, String>(coroutinesDispatcherProvider.io) {
    override suspend fun execute(parameters: String) = eventRepository.getStatus(parameters)
}

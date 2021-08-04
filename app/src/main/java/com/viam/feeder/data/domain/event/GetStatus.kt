package com.viam.feeder.data.domain.event

import com.viam.feeder.core.domain.UseCase
import com.viam.feeder.core.network.CoroutinesDispatcherProvider
import com.viam.feeder.data.repository.EventRepository
import javax.inject.Inject


class GetStatus @Inject constructor(
    coroutinesDispatcherProvider: CoroutinesDispatcherProvider,
    private val eventRepository: EventRepository
) : UseCase<String, String>(coroutinesDispatcherProvider.io) {
    override suspend fun execute(parameters: String) = eventRepository.getStatus(parameters)
}

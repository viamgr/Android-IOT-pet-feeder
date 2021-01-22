package com.viam.feeder.data.domain.event

import com.viam.feeder.core.domain.UseCase
import com.viam.feeder.core.network.CoroutinesDispatcherProvider
import com.viam.feeder.data.repository.EventRepository
import dagger.hilt.android.scopes.ActivityScoped
import javax.inject.Inject

@ActivityScoped
class SetStatus @Inject constructor(
    coroutinesDispatcherProvider: CoroutinesDispatcherProvider,
    private val eventRepository: EventRepository
) : UseCase<Status, Unit>(coroutinesDispatcherProvider.io) {
    override suspend fun execute(parameters: Status) =
        eventRepository.setStatus(parameters.key, parameters.value)
}

data class Status(val key: String, val value: String)
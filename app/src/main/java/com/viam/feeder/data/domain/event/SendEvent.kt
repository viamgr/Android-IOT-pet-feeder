package com.viam.feeder.data.domain.event

import com.viam.feeder.core.domain.UseCase
import com.viam.feeder.core.network.CoroutinesDispatcherProvider
import com.viam.feeder.data.models.KeyValue
import com.viam.feeder.data.repository.EventRepository
import dagger.hilt.android.scopes.ActivityScoped
import javax.inject.Inject

@ActivityScoped
class SendEvent @Inject constructor(
    coroutinesDispatcherProvider: CoroutinesDispatcherProvider,
    private val eventRepository: EventRepository
) : UseCase<KeyValue, Unit>(coroutinesDispatcherProvider.io) {
    override suspend fun execute(parameters: KeyValue) = eventRepository.sendEvent(parameters)
}
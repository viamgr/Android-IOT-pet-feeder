package com.viam.feeder.data.domain.timer

import com.viam.feeder.core.domain.UseCase
import com.viam.feeder.core.network.CoroutinesDispatcherProvider
import com.viam.feeder.data.models.ClockTimer
import com.viam.feeder.data.repository.TimerRepository
import dagger.hilt.android.scopes.ActivityScoped
import javax.inject.Inject

@ActivityScoped
class DeleteTimer @Inject constructor(
    coroutinesDispatcherProvider: CoroutinesDispatcherProvider,
    private val timerRepository: TimerRepository
) : UseCase<ClockTimer, Unit>(coroutinesDispatcherProvider.io) {
    override suspend fun execute(parameters: ClockTimer) = timerRepository.delete(parameters)
}
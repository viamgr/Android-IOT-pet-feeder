package com.viam.feeder.domain.usecase.device

import com.viam.feeder.domain.base.CoroutinesDispatcherProvider
import com.viam.feeder.domain.base.UseCase
import com.viam.feeder.domain.repositories.socket.DeviceRepository
import com.viam.feeder.model.Device
import javax.inject.Inject

class AddDevice @Inject constructor(
    coroutinesDispatcherProvider: CoroutinesDispatcherProvider,
    private val deviceRepository: DeviceRepository,
) : UseCase<Device, Unit>(coroutinesDispatcherProvider.io) {
    override suspend fun execute(parameters: Device) {
        return deviceRepository.insertAll(parameters)
    }
}
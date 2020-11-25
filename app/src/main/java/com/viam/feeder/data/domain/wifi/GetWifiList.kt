package com.viam.feeder.data.domain.wifi

import com.viam.feeder.core.domain.UseCase
import com.viam.feeder.core.network.CoroutinesDispatcherProvider
import com.viam.feeder.data.models.WifiDevice
import com.viam.feeder.data.repository.WifiRepository
import dagger.hilt.android.scopes.ActivityScoped
import javax.inject.Inject

@ActivityScoped
class GetWifiList @Inject constructor(
    coroutinesDispatcherProvider: CoroutinesDispatcherProvider,
    private val wifiRepository: WifiRepository
) : UseCase<Unit?, List<WifiDevice>>(coroutinesDispatcherProvider.io) {
    override suspend fun execute(parameters: Unit?) = wifiRepository.getList()
}
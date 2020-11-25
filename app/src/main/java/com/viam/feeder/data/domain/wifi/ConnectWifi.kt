package com.viam.feeder.data.domain.wifi

import com.viam.feeder.core.domain.UseCase
import com.viam.feeder.core.network.CoroutinesDispatcherProvider
import com.viam.feeder.data.repository.WifiRepository
import dagger.hilt.android.scopes.ActivityScoped
import javax.inject.Inject

@ActivityScoped
class ConnectWifi @Inject constructor(
    coroutinesDispatcherProvider: CoroutinesDispatcherProvider,
    private val wifiRepository: WifiRepository
) : UseCase<WifiAuthentication, Unit>(coroutinesDispatcherProvider.io) {
    override suspend fun execute(parameters: WifiAuthentication) =
        wifiRepository.connect(parameters.ssid, parameters.password)
}

data class WifiAuthentication(val ssid: String, val password: String)
package com.viam.feeder.data.domain.config

import com.viam.feeder.core.network.CoroutinesDispatcherProvider
import com.viam.feeder.data.repository.UploadRepository
import com.viam.feeder.data.repository.WifiRepository
import com.viam.feeder.data.storage.ConfigStorageImpl
import dagger.hilt.android.scopes.ActivityScoped
import javax.inject.Inject

@ActivityScoped
class SetWifiCredentials @Inject constructor(
    coroutinesDispatcherProvider: CoroutinesDispatcherProvider,
    private val configStorage: ConfigStorageImpl,
    uploadRepository: UploadRepository,
    private val wifiRepository: WifiRepository,
) : BaseSetConfig<WifiAuthentication>(
    coroutinesDispatcherProvider.io,
    configStorage,
    uploadRepository
) {
    override suspend fun setConfigField(value: WifiAuthentication) {
        configStorage.wifiSsid = value.ssid
        configStorage.wifiPassword = value.password
    }

    override suspend fun execute(parameters: WifiAuthentication) {
        super.execute(parameters)
        wifiRepository.connect()
    }
}

data class WifiAuthentication(val ssid: String, val password: String)
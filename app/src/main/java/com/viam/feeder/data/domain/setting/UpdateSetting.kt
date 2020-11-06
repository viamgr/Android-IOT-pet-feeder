package com.viam.feeder.data.domain.setting

import com.viam.feeder.core.domain.UseCase
import com.viam.feeder.core.network.CoroutinesDispatcherProvider
import com.viam.feeder.data.models.KeyValue
import com.viam.feeder.data.repository.SettingRepository
import dagger.hilt.android.scopes.ActivityScoped
import javax.inject.Inject

@ActivityScoped
class UpdateSetting @Inject constructor(
    coroutinesDispatcherProvider: CoroutinesDispatcherProvider,
    private val settingRepository: SettingRepository
) : UseCase<KeyValue, KeyValue>(coroutinesDispatcherProvider.io) {
    override suspend fun execute(parameters: KeyValue) = settingRepository.update(parameters)
}
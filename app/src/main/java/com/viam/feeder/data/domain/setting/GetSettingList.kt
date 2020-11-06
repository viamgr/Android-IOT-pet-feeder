package com.viam.feeder.data.domain.setting

import com.viam.feeder.core.domain.UseCase
import com.viam.feeder.core.network.CoroutinesDispatcherProvider
import com.viam.feeder.data.models.KeyValue
import com.viam.feeder.data.repository.SettingRepository
import dagger.hilt.android.scopes.ActivityScoped
import javax.inject.Inject

@ActivityScoped
class GetSettingList @Inject constructor(
    coroutinesDispatcherProvider: CoroutinesDispatcherProvider,
    private val settingRepository: SettingRepository
) : UseCase<Pair<String, String>, List<KeyValue>>(coroutinesDispatcherProvider.io) {
    override suspend fun execute(parameters: Pair<String, String>) = settingRepository.list()
}
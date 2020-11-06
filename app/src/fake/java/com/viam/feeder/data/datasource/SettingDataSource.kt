package com.viam.feeder.data.datasource

import com.viam.feeder.constants.SETTING_INTERVAL
import com.viam.feeder.constants.SETTING_VOLUME
import com.viam.feeder.data.models.KeyValue
import dagger.hilt.android.scopes.ActivityScoped
import kotlinx.coroutines.delay
import javax.inject.Inject

@ActivityScoped
class SettingDataSource @Inject constructor() {

    private val settings =
        mutableListOf(KeyValue(SETTING_INTERVAL, 20000L), KeyValue(SETTING_VOLUME, 3000L))

    suspend fun list(): List<KeyValue> {
        delay(2000)
        return settings
    }

    suspend fun update(setting: KeyValue): KeyValue {
        delay(2000)
        val firstOrNull = settings.firstOrNull { it == setting }
        return if (firstOrNull == null) {
            val newItem = KeyValue(setting.key, setting.value)
            settings.add(newItem)
            newItem
        } else {
            firstOrNull.apply {
                key = setting.key
                value = setting.value
            }
        }

    }
}
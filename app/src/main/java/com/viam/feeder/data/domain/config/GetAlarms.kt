package com.viam.feeder.data.domain.config

import androidx.lifecycle.map
import com.viam.feeder.core.domain.LiveDataUseCase
import com.viam.feeder.data.models.ClockTimer
import com.viam.feeder.data.storage.ConfigFields
import dagger.hilt.android.scopes.ActivityScoped
import javax.inject.Inject

@ActivityScoped
class GetAlarms @Inject constructor(
    private val configFields: ConfigFields,
) : LiveDataUseCase<List<ClockTimer>>() {
    override fun getField() = configFields.alarms.map {
        it.mapIndexed { index, value ->
            val splitCron = value.split(" ")
            ClockTimer(id = index, hour = splitCron[2].toInt(), minute = splitCron[1].toInt())
        }
    }
}
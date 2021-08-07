package com.viam.feeder.domain.usecase.config

import androidx.lifecycle.map
import com.viam.feeder.domain.base.LiveDataUseCase
import com.viam.feeder.domain.repositories.system.ConfigFields
import com.viam.feeder.model.ClockTimer
import javax.inject.Inject


class GetAlarms @Inject constructor(
    private val configFields: ConfigFields,
) : LiveDataUseCase<List<ClockTimer>>() {
    override fun getField() = configFields.getAlarms().map {
        it.mapIndexed { index, value ->
            val splitCron = value.split(" ")
            ClockTimer(id = index, hour = splitCron[2].toInt(), minute = splitCron[1].toInt())
        }
    }
}
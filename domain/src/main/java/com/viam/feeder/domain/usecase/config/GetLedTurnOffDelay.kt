package com.viam.feeder.domain.usecase.config

import com.viam.feeder.domain.base.LiveDataUseCase
import com.viam.feeder.domain.repositories.system.ConfigFields
import javax.inject.Inject


class GetLedTurnOffDelay @Inject constructor(
    private val configFields: ConfigFields
) : LiveDataUseCase<Int>() {
    override fun getField() = configFields.getLedTurnOffDelay()
}
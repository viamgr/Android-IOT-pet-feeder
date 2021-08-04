package com.viam.feeder.data.domain.config

import com.viam.feeder.core.domain.LiveDataUseCase
import com.viam.feeder.data.storage.ConfigFields
import javax.inject.Inject


class GetLedTurnOffDelay @Inject constructor(
    private val configFields: ConfigFields
) : LiveDataUseCase<Int>() {
    override fun getField() = configFields.ledTurnOffDelay
}
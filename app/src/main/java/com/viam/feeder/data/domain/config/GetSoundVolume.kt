package com.viam.feeder.data.domain.config

import com.viam.feeder.core.domain.LiveDataUseCase
import com.viam.feeder.data.storage.ConfigFields
import javax.inject.Inject


class GetSoundVolume @Inject constructor(
    private val configFields: ConfigFields
) : LiveDataUseCase<Float>() {
    override fun getField() = configFields.soundVolume
}
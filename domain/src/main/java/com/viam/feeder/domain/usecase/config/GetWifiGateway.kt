package com.viam.feeder.domain.usecase.config

import com.viam.feeder.domain.base.LiveDataUseCase
import com.viam.feeder.domain.repositories.system.ConfigFields
import javax.inject.Inject

class GetWifiGateway @Inject constructor(
    private val configFields: ConfigFields
) : LiveDataUseCase<String>() {
    override fun getField() = configFields.getWifiGateway()
}
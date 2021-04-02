package com.viam.feeder.data.domain.config

import com.viam.feeder.core.domain.LiveDataUseCase
import com.viam.feeder.data.storage.ConfigFields
import dagger.hilt.android.scopes.ActivityScoped
import javax.inject.Inject

@ActivityScoped
class GetLedState @Inject constructor(
    private val configFields: ConfigFields
) : LiveDataUseCase<Int>() {
    override fun getField() = configFields.ledState
}
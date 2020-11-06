package com.viam.feeder.data.datasource

import com.viam.feeder.data.models.KeyValue
import dagger.hilt.android.scopes.ActivityScoped
import kotlinx.coroutines.delay
import javax.inject.Inject

@ActivityScoped
class EventDataSource @Inject constructor() {

    suspend fun sendEvent(event: KeyValue) {
        delay(2000)
    }
}
package com.viam.feeder.data.datasource

import com.viam.feeder.data.models.KeyValue
import com.viam.feeder.data.utils.fakeRequest
import dagger.hilt.android.scopes.ActivityScoped
import javax.inject.Inject

@ActivityScoped
class EventDataSource @Inject constructor() {

    suspend fun sendEvent(event: KeyValue) = fakeRequest {

    }
}
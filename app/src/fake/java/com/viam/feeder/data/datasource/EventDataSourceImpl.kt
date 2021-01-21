package com.viam.feeder.data.datasource

import android.content.Context
import com.viam.feeder.data.utils.fakeRequest
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class EventDataSourceImpl @Inject constructor(@ApplicationContext private val context: Context) :
    EventDataSource {
    override suspend fun sendEvent(event: String) = fakeRequest(context) {

    }
}
package com.viam.feeder.data.datasource

import android.content.Context
import com.viam.feeder.constants.STATUS_ACCESS_POINT
import com.viam.feeder.constants.STATUS_TIME
import com.viam.feeder.data.utils.fakeRequest
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class EventDataSourceImpl @Inject constructor(@ApplicationContext private val context: Context) :
    EventDataSource {
    override suspend fun sendEvent(event: String) = fakeRequest(context) {

    }

    override suspend fun setState(key: String, value: Any) = fakeRequest(context) {

    }

    override suspend fun getState(key: String): String = fakeRequest(context) {
        when (key) {
            STATUS_TIME -> {
                System.currentTimeMillis().toString()
            }
            STATUS_ACCESS_POINT -> {
                "1"
            }
            else -> {
                ""
            }
        }
    }
}
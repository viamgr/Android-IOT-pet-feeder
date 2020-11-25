package com.viam.feeder.ui.dashboard

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.ViewModel
import com.viam.feeder.constants.EVENT_TRIGGER
import com.viam.feeder.core.domain.toLiveTask
import com.viam.feeder.data.domain.event.SendEvent
import com.viam.feeder.data.models.KeyValue

class DashboardViewModel @ViewModelInject constructor(sendEvent: SendEvent) : ViewModel() {

    val sendRequestEvent = sendEvent.toLiveTask()
    fun sendTriggerEvent() {
        sendRequestEvent.post(KeyValue(EVENT_TRIGGER))
    }
}
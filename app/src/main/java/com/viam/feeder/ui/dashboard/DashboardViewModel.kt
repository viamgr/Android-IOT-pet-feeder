package com.viam.feeder.ui.dashboard

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.ViewModel
import com.viam.feeder.constants.EVENT_COMPOSITE_FEEDING
import com.viam.feeder.constants.EVENT_FEEDING
import com.viam.feeder.constants.EVENT_LED_TIMER
import com.viam.feeder.constants.EVENT_PLAY_FEEDING_AUDIO
import com.viam.feeder.core.domain.utils.toLiveTask
import com.viam.feeder.data.domain.event.SendEvent

class DashboardViewModel @ViewModelInject constructor(sendEvent: SendEvent) : ViewModel() {

    val sendRequestEvent = sendEvent.toLiveTask()
    fun sendCompositeFeedingEvent() {
        sendRequestEvent.post(EVENT_COMPOSITE_FEEDING)
    }

    fun sendLightEvent() {
        sendRequestEvent.post(EVENT_LED_TIMER)
    }

    fun sendFeedingEvent() {
        sendRequestEvent.post(EVENT_FEEDING)
    }

    fun sendCallingEvent() {
        sendRequestEvent.post(EVENT_PLAY_FEEDING_AUDIO)
    }
}
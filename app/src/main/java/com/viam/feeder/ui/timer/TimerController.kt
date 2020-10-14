package com.viam.feeder.ui.timer

import com.airbnb.epoxy.TypedEpoxyController
import com.viam.feeder.clockTimer
import com.viam.feeder.data.models.ClockTimer

class TimerController : TypedEpoxyController<List<ClockTimer>>() {
    lateinit var clickListener: (ClockTimer) -> Unit
    override fun buildModels(data: List<ClockTimer>?) {
        data?.forEach { clockTimer: ClockTimer ->
            clockTimer {
                id(clockTimer.id)
                hour(clockTimer.hour)
                minute(clockTimer.minute)
                time(clockTimer.time)
                removeListener { _ ->
                    clickListener(clockTimer)
                }
            }
        }
    }
}
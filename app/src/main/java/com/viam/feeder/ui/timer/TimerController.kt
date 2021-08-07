package com.viam.feeder.ui.timer

import com.airbnb.epoxy.TypedEpoxyController
import com.viam.feeder.R
import com.viam.feeder.clockTimer

class TimerController : TypedEpoxyController<List<com.viam.feeder.model.ClockTimer>>() {
    lateinit var clickListener: (com.viam.feeder.model.ClockTimer) -> Unit
    override fun buildModels(data: List<com.viam.feeder.model.ClockTimer>?) {
        data?.forEach { clockTimer: com.viam.feeder.model.ClockTimer ->
            clockTimer {
                id(clockTimer.id)
                hour(if (clockTimer.hour > 12) clockTimer.hour % 12 else clockTimer.hour)
                minute(clockTimer.minute)
                time(if (clockTimer.hour < 12) R.string.am else R.string.pm)
                removeListener { _ ->
                    this@TimerController.clickListener(clockTimer)
                }
            }
        }
    }
}
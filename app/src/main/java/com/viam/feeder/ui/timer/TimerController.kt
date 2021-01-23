package com.viam.feeder.ui.timer

import com.airbnb.epoxy.TypedEpoxyController
import com.viam.feeder.R
import com.viam.feeder.clockTimer
import com.viam.feeder.data.models.ClockTimer

class TimerController : TypedEpoxyController<List<ClockTimer>>() {
    lateinit var clickListener: (ClockTimer) -> Unit
    override fun buildModels(data: List<ClockTimer>?) {
        data?.forEach { clockTimer: ClockTimer ->
            clockTimer {
                id(clockTimer.id)
                hour(if (clockTimer.hour > 12) clockTimer.hour % 12 else clockTimer.hour)
                minute(clockTimer.minute)
                time(if (clockTimer.hour < 12) R.string.am else R.string.pm)
                removeListener { _ ->
                    clickListener(clockTimer)
                }
            }
        }
    }
}
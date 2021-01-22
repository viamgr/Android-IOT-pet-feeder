package com.viam.feeder.data.models

import com.viam.feeder.R

class ClockTimer(var id: Int = 0, hour: Int, var minute: Int, var time: Int = R.string.am) {
    var hour: Int = hour
        set(value) {
            field = if (value == 12) 12 else value % 12
            time = if (value < 12) R.string.am else R.string.pm
        }

    init {
        this.hour = hour
    }
}
package com.viam.feeder.data.models

import com.viam.feeder.R

class ClockTimer(var id: Long = 0, hour: Int, var minute: Int, var time: Int = R.string.am) {
    var hour: Int = hour
        set(value) {
            field = value % 12
            time = if (value < 13) R.string.am else R.string.pm
        }

    init {
        this.hour = hour
    }
}
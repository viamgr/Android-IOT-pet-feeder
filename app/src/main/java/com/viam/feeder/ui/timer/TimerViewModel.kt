package com.viam.feeder.ui.timer

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.viam.feeder.R
import com.viam.feeder.core.livedata.Event
import com.viam.feeder.models.ClockTimer

class TimerViewModel @ViewModelInject constructor() : ViewModel() {


    private val _clockTimer = MutableLiveData(
        listOf(
            ClockTimer(1, "1:10", R.string.am),
            ClockTimer(2, "9:45", R.string.pm),
            ClockTimer(3, "1:10", R.string.am),
            ClockTimer(4, "9:45", R.string.pm),
            ClockTimer(5, "1:10", R.string.am),
            ClockTimer(6, "9:45", R.string.pm),
            ClockTimer(7, "1:10", R.string.am),
            ClockTimer(8, "9:45", R.string.pm),
            ClockTimer(88, "1:10", R.string.am),
            ClockTimer(9, "9:45", R.string.pm),
        )
    )
    val clockTimer: LiveData<List<ClockTimer>> = _clockTimer

    private val _openTimerDialog = MutableLiveData<Event<Unit>>()
    val openTimerDialog: LiveData<Event<Unit>> = _openTimerDialog
    fun onClockTimerClicked(position: Long) {

    }

    fun onClickAddClock() {
        _openTimerDialog.postValue(Event(Unit))
    }
}
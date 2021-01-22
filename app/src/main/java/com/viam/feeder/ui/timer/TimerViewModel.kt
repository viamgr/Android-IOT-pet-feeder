package com.viam.feeder.ui.timer

import android.text.format.DateFormat
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.viam.feeder.constants.STATUS_TIME
import com.viam.feeder.core.dataOrNull
import com.viam.feeder.core.domain.utils.toLiveTask
import com.viam.feeder.core.livedata.Event
import com.viam.feeder.core.task.compositeTask
import com.viam.feeder.data.domain.config.GetAlarms
import com.viam.feeder.data.domain.config.SetAlarms
import com.viam.feeder.data.domain.event.GetStatus
import com.viam.feeder.data.domain.event.SetStatus
import com.viam.feeder.data.domain.event.Status
import com.viam.feeder.data.models.ClockTimer
import java.util.*

class TimerViewModel @ViewModelInject constructor(
    setAlarms: SetAlarms,
    getAlarms: GetAlarms,
    getStatus: GetStatus,
    setStatus: SetStatus,
) : ViewModel() {

    private val _time = MutableLiveData<String>("0:00")
    val time: LiveData<String> = _time

    private val _date = MutableLiveData<String>("1 DECEMBER, 1970")
    val date: LiveData<String> = _date

    private val _ampm = MutableLiveData<String>("AM")
    val ampm: LiveData<String> = _ampm

    private val _showTimeSettingMenu = MutableLiveData<Event<Unit>>()
    val showTimeSettingMenu: LiveData<Event<Unit>> = _showTimeSettingMenu

    val getStatusTask = getStatus.toLiveTask().also {
        it.post(STATUS_TIME)
    }.onSuccess {
        it?.let {
            val timeInMillis = it.toLong()
            val inDate = Date(timeInMillis)
            _date.value = DateFormat.format("EEE, MMMM dd, yyyy", inDate).toString()
            _time.value = DateFormat.format("h:mm", inDate).toString()
            _ampm.value = DateFormat.format("aa", inDate).toString()
        }
    }

    val setStatusTask = setStatus.toLiveTask()

    val timeCompositeTask = compositeTask(
        setStatusTask, getStatusTask
    )
    private val _openTimerDialog = MutableLiveData<Event<Unit>>()
    val openTimerDialog: LiveData<Event<Unit>> = _openTimerDialog

    private var list = mutableListOf<ClockTimer>()
    val getTimerListTask = getAlarms.toLiveTask().also {
        it.post(Unit)
    }.onSuccess {
        it?.let {
            list = it.toMutableList()
        }
    }

    private val addTimerTask = setAlarms.toLiveTask().onSuccess {
        getTimerListTask.post(Unit)
    }


    val compositeTask = compositeTask(
        addTimerTask,
        getTimerListTask,
    )

    private val _timerMode = MutableLiveData<Int>()
    val timerMode: LiveData<Int> = _timerMode


    fun removeTimer(timer: ClockTimer) {
        getTimerList()?.let { newList ->
            newList.removeAll { timer.id == it.id }
            addTimerTask.post(newList)
        }
    }

    fun onClickAddClock() {
        _openTimerDialog.postValue(Event(Unit))
    }

    fun onTimeSet(timeInMillis: Long) {
        setStatusTask.post(Status(STATUS_TIME, timeInMillis.toString()))
    }

    fun onAddTime(newHour: Int, newMinute: Int) {
        list.let { newList ->
            newList.add(ClockTimer(hour = newHour, minute = newMinute))
            addTimerTask.post(newList)
        }

    }

    fun onTimeSettingClicked() {
        _showTimeSettingMenu.value = Event(Unit)
    }

    private fun getTimerList(): MutableList<ClockTimer>? =
        getTimerListTask.state().dataOrNull()?.toMutableList()

    fun onTabChanged(position: Int?) {
        _timerMode.value = if (position == 0) TIMER_MODE_SCHEDULING else TIMER_MODE_PERIODIC
    }


    companion object {
        const val TIMER_MODE_SCHEDULING = 0
        const val TIMER_MODE_PERIODIC = 1
    }

}
package com.viam.feeder.ui.timer

import android.text.format.DateFormat
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.viam.feeder.constants.STATUS_TIME
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

    private val _time = MutableLiveData("0:00")
    val time: LiveData<String> = _time

    private val _date = MutableLiveData("1 DECEMBER, 1970")
    val date: LiveData<String> = _date

    private val _ampm = MutableLiveData("AM")
    val ampm: LiveData<String> = _ampm

    private val _showTimeSettingMenu = MutableLiveData<Event<Unit>>()
    val showTimeSettingMenu: LiveData<Event<Unit>> = _showTimeSettingMenu

    private val getStatusTask = getStatus.toLiveTask {
        onSuccess {
            it?.let {
                val timeInMillis = it.toLong() * 1000
                val inDate = Date(timeInMillis)
                _date.value = DateFormat.format("EEE, MMMM dd, yyyy", inDate).toString()
                _time.value = DateFormat.format("h:mm", inDate).toString()
                _ampm.value = DateFormat.format("aa", inDate).toString()
            }
        }
    }.execute(STATUS_TIME)
    val setStatusTask = setStatus.toLiveTask {
        onSuccess {
            getStatusTask.execute(STATUS_TIME)
        }
    }

    val timeCompositeTask = compositeTask(
        setStatusTask, getStatusTask
    )
    private val _openTimerDialog = MutableLiveData<Event<Unit>>()
    val openTimerDialog: LiveData<Event<Unit>> = _openTimerDialog

    private val _timerList = MutableLiveData<List<ClockTimer>>()
    val timerList: LiveData<List<ClockTimer>> = _timerList

    private val getTimerListTask = getAlarms.toLiveTask {
        onSuccess {
            it?.let {
                _timerList.postValue(it.toMutableList())
            }
        }
    }.execute(Unit)

    val addTimerTask = setAlarms.toLiveTask {
        onSuccess {
            getTimerListTask.execute(Unit)
        }
    }

    val compositeTask = compositeTask(
        addTimerTask,
        getTimerListTask,
    )

    private val _timerMode = MutableLiveData<Int>()
    val timerMode: LiveData<Int> = _timerMode


    fun removeTimer(timer: ClockTimer) {
        timerList.value?.let { list ->
            val newList = list.toMutableList()
            newList.removeAll { timer.id == it.id }
            addTimerTask.execute(newList)
        }
    }

    fun onClickAddClock() {
        _openTimerDialog.postValue(Event(Unit))
    }

    fun onTimeSet(timeInMillis: Long) {
        setStatusTask.execute(Status(STATUS_TIME, (timeInMillis / 1000).toInt().toString()))
    }

    fun onAddTime(newHour: Int, newMinute: Int) {
        timerList.value?.let { it ->
            val newList = it.toMutableList()
            newList.add(ClockTimer(hour = newHour, minute = newMinute))
            addTimerTask.execute(newList)
        }

    }

    fun onTimeSettingClicked() {
        _showTimeSettingMenu.value = Event(Unit)
    }

/*
    fun onTabChanged(position: Int?) {
        _timerMode.value = if (position == 0) TIMER_MODE_SCHEDULING else TIMER_MODE_PERIODIC
    }
*/


    companion object {
        const val TIMER_MODE_SCHEDULING = 0
//        const val TIMER_MODE_PERIODIC = 1
    }

}
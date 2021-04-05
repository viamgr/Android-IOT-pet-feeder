package com.viam.feeder.ui.timer

import android.text.format.DateFormat
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.viam.feeder.constants.TIME_GET
import com.viam.feeder.constants.TIME_IS
import com.viam.feeder.constants.TIME_SET
import com.viam.feeder.core.livedata.Event
import com.viam.feeder.core.onSuccess
import com.viam.feeder.core.utility.launchInScope
import com.viam.feeder.data.domain.config.GetAlarms
import com.viam.feeder.data.domain.config.SetAlarms
import com.viam.feeder.data.domain.event.GetLongValue
import com.viam.feeder.data.domain.event.SendEvent
import com.viam.feeder.data.domain.event.SendLongValue
import com.viam.feeder.data.models.ClockTimer
import com.viam.feeder.data.models.KeyValueMessage
import kotlinx.coroutines.flow.collect
import java.util.*

class TimerViewModel @ViewModelInject constructor(
    getAlarms: GetAlarms,
    private val getTime: GetLongValue,
    private val requestGetTime: SendEvent,
    private val setAlarms: SetAlarms,
    private val setTime: SendLongValue
) : ViewModel() {

    private val _time = MutableLiveData("0:00")
    val time: LiveData<String> = _time

    private val _date = MutableLiveData("1 DECEMBER, 1970")
    val date: LiveData<String> = _date

    private val _ampm = MutableLiveData("AM")
    val ampm: LiveData<String> = _ampm

    private val _showTimeSettingMenu = MutableLiveData<Event<Unit>>()
    val showTimeSettingMenu: LiveData<Event<Unit>> = _showTimeSettingMenu

    private val _openTimerDialog = MutableLiveData<Event<Unit>>()
    val openTimerDialog: LiveData<Event<Unit>> = _openTimerDialog

    val timerList: LiveData<List<ClockTimer>> = getAlarms()

    private val _timerMode = MutableLiveData<Int>()
    val timerMode: LiveData<Int> = _timerMode

    init {
        requestTime()
    }

    private fun requestTime() {
        launchInScope {
            getTime(TIME_IS).also {
                requestGetTime(TIME_GET)
            }.collect { resource ->
                resource.onSuccess {
                    val timeInMillis = (it ?: 0) * 1000
                    val inDate = Date(timeInMillis)
                    _date.value = DateFormat.format("EEE, MMMM dd, yyyy", inDate).toString()
                    _time.value = DateFormat.format("h:mm", inDate).toString()
                    _ampm.value = DateFormat.format("aa", inDate).toString()
                }
            }
        }
    }

    fun removeTimer(timer: ClockTimer) = launchInScope {
        timerList.value?.let { list ->
            val newList = list.toMutableList()
            newList.removeAll { timer.id == it.id }
            setAlarms(newList)
        }
    }

    fun onClickAddClock() {
        _openTimerDialog.postValue(Event(Unit))
    }

    fun onTimeSet(timeInMillis: Long) = launchInScope {
        setTime(KeyValueMessage(TIME_SET, (timeInMillis / 1000)))
        launchInScope {
            requestGetTime(TIME_GET)
        }
    }

    fun onAddTime(newHour: Int, newMinute: Int) = launchInScope {
        timerList.value?.let { it ->
            val newList = it.toMutableList()
            newList.add(ClockTimer(hour = newHour, minute = newMinute))
            setAlarms(newList)
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
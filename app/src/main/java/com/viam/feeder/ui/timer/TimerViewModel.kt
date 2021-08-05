package com.viam.feeder.ui.timer

import android.text.format.DateFormat
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.part.livetaskcore.livatask.combine
import com.part.livetaskcore.usecases.asLiveTask
import com.viam.feeder.constants.TIME_GET
import com.viam.feeder.constants.TIME_IS
import com.viam.feeder.constants.TIME_SET
import com.viam.feeder.core.livedata.Event
import com.viam.feeder.core.utility.launchInScope
import com.viam.feeder.data.domain.config.GetAlarms
import com.viam.feeder.data.domain.config.SetAlarms
import com.viam.feeder.data.domain.event.GetLongValue
import com.viam.feeder.data.domain.event.SendEvent
import com.viam.feeder.data.domain.event.SendLongValue
import com.viam.feeder.data.models.ClockTimer
import com.viam.feeder.data.models.KeyValueMessage
import dagger.hilt.android.lifecycle.HiltViewModel
import java.util.*
import javax.inject.Inject

@HiltViewModel
class TimerViewModel @Inject constructor(
    getAlarms: GetAlarms,
    onLongValue: GetLongValue,
    requestGetTime: SendEvent,
    setAlarms: SetAlarms,
    setTime: SendLongValue
) : ViewModel() {

    val setTimeTask = setTime.asLiveTask {
        onSuccess<Unit> { requestGetTime() }
    }
    private val setAlarmsTask = setAlarms.asLiveTask()
    private val requestGetTimeTask = requestGetTime.asLiveTask()
    private val onLongValueTask = onLongValue.asLiveTask {
        onSuccess<Long> {
            val timeInMillis = it * 1000
            val inDate = Date(timeInMillis)
            _date.value = DateFormat.format("EEE, MMMM dd, yyyy", inDate).toString()
            _time.value = DateFormat.format("h:mm", inDate).toString()
            _ampm.value = DateFormat.format("aa", inDate).toString()
        }
    }
    val combined = combine(
        setTimeTask,
        setAlarmsTask,
        requestGetTimeTask,
        onLongValueTask,
    )
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
        setTimeListener().also {
            requestGetTime()
        }
    }

    private fun requestGetTime() = launchInScope {
        requestGetTimeTask(TIME_GET)
    }

    private fun setTimeListener() = launchInScope {
        onLongValueTask(TIME_IS)
    }

    fun removeTimer(timer: ClockTimer) = launchInScope {
        timerList.value?.let { list ->
            val newList = list.toMutableList()
            newList.removeAll { timer.id == it.id }
            setAlarmsTask(newList)
        }
    }

    fun onClickAddClock() {
        _openTimerDialog.postValue(Event(Unit))
    }

    fun onTimeSet(timeInMillis: Long) = launchInScope {
        setTimeTask(KeyValueMessage(TIME_SET, (timeInMillis / 1000)))
    }

    fun onAddTime(newHour: Int, newMinute: Int) = launchInScope {
        (timerList.value ?: emptyList()).let {
            val newList = it.toMutableList()
            newList.add(ClockTimer(hour = newHour, minute = newMinute))
            setAlarmsTask(newList)
        }
    }

    fun onTimeSettingClicked() {
        _showTimeSettingMenu.value = Event(Unit)
    }

    companion object {
        const val TIMER_MODE_SCHEDULING = 0
    }

}
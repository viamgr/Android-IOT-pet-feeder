package com.viam.feeder.ui.timer

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.viam.feeder.constants.SETTING_FEEDING_INTERVAL
import com.viam.feeder.core.dataOrNull
import com.viam.feeder.core.domain.utils.toLiveTask
import com.viam.feeder.core.livedata.Event
import com.viam.feeder.core.task.compositeTask
import com.viam.feeder.data.domain.config.GetAlarms
import com.viam.feeder.data.domain.config.SetAlarms
import com.viam.feeder.data.domain.event.SendEvent
import com.viam.feeder.data.domain.timer.DeleteTimer
import com.viam.feeder.data.models.ClockTimer
import com.viam.feeder.data.models.KeyValue

class TimerViewModel @ViewModelInject constructor(
    setAlarms: SetAlarms,
    deleteTimer: DeleteTimer,
    getAlarms: GetAlarms,
    sendEvent: SendEvent
) : ViewModel() {

    private val _openTimerDialog = MutableLiveData<Event<Unit>>()
    val openTimerDialog: LiveData<Event<Unit>> = _openTimerDialog

    private val _currentValue = MutableLiveData(DEFAULT_VALUE)
    val currentValue: LiveData<Int> = _currentValue

    private val getTimerListTask = getAlarms.toLiveTask {
        onSuccess {
            controller.setData(it)
        }
    }.also {
        it.post(Unit)
    }

    private val addTimerTask = setAlarms.toLiveTask {
        onSuccess {
            getTimerListTask.post(Unit)
        }
    }

    private val deleteTimerTask = deleteTimer.toLiveTask {
        onSuccess {
            getTimerListTask.post(Unit)
        }
    }

    private val sendEventTask = sendEvent.toLiveTask {
        debounce(1000)
    }

    init {
        sendEventTask.asLiveData().addSource(_currentValue) {
            sendEventTask.postWithCancel(KeyValue(SETTING_FEEDING_INTERVAL, it))
        }
    }

    val compositeTask = compositeTask(
        addTimerTask,
        getTimerListTask,
        deleteTimerTask,
        sendEventTask,
    )

    private val _timerMode = MutableLiveData<Int>()
    val timerMode: LiveData<Int> = _timerMode

    val controller = TimerController()
        .also { timerController ->
            timerController.clickListener = { clockTimer ->
                removeTimer(clockTimer)
            }
        }

    private fun removeTimer(timer: ClockTimer) {
        deleteTimerTask.post(timer)
    }

    fun onClickAddClock() {
        _openTimerDialog.postValue(Event(Unit))
    }

    fun onTimeSet(newHour: Int, newMinute: Int) {
        getTimerListTask.state().dataOrNull()?.let {
            val newList = it.toMutableList()
            newList.add(ClockTimer(hour = newHour, minute = newMinute))
            addTimerTask.post(newList)
        }

    }


    fun onUpClicked() {
        _currentValue.value =
            _currentValue.value?.plus(1)?.coerceAtMost(MAX_VALUE)
    }

    fun onDownClicked() {
        _currentValue.value =
            _currentValue.value?.minus(1)?.coerceAtLeast(MIN_VALUE)

    }

    fun onTabChanged(position: Int?) {
        _timerMode.value = if (position == 0) TIMER_MODE_SCHEDULING else TIMER_MODE_PERIODIC
    }

    fun onValueChanged(value: Float) {
        _currentValue.value = value.toInt()
    }

    companion object {
        const val TIMER_MODE_SCHEDULING = 0
        const val TIMER_MODE_PERIODIC = 1
        const val MAX_VALUE = 24 * 60
        const val MIN_VALUE = 1
        const val DEFAULT_VALUE = 4 * 60
    }

}
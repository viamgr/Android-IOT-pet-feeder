package com.viam.feeder.ui.timer

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.viam.feeder.core.dataOrNull
import com.viam.feeder.core.domain.utils.toLiveTask
import com.viam.feeder.core.livedata.Event
import com.viam.feeder.core.task.compositeTask
import com.viam.feeder.data.domain.config.GetAlarms
import com.viam.feeder.data.domain.config.SetAlarms
import com.viam.feeder.data.models.ClockTimer

class TimerViewModel @ViewModelInject constructor(
    setAlarms: SetAlarms,
    getAlarms: GetAlarms,
) : ViewModel() {

    private val _openTimerDialog = MutableLiveData<Event<Unit>>()
    val openTimerDialog: LiveData<Event<Unit>> = _openTimerDialog

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


    val compositeTask = compositeTask(
        addTimerTask,
        getTimerListTask,
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
        getTimerList()?.let { newList ->
            newList.removeAll { timer.id == it.id }
            addTimerTask.post(newList)
        }
    }

    fun onClickAddClock() {
        _openTimerDialog.postValue(Event(Unit))
    }

    fun onTimeSet(newHour: Int, newMinute: Int) {
        getTimerList()?.let { newList ->
            newList.add(ClockTimer(hour = newHour, minute = newMinute))
            addTimerTask.post(newList)
        }

    }

    private fun getTimerList() = getTimerListTask.state().dataOrNull()?.toMutableList()

    fun onTabChanged(position: Int?) {
        _timerMode.value = if (position == 0) TIMER_MODE_SCHEDULING else TIMER_MODE_PERIODIC
    }


    companion object {
        const val TIMER_MODE_SCHEDULING = 0
        const val TIMER_MODE_PERIODIC = 1
    }

}
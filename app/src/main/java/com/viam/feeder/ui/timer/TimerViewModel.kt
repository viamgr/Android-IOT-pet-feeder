package com.viam.feeder.ui.timer

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.viam.feeder.core.Resource
import com.viam.feeder.core.dataOrNull
import com.viam.feeder.core.livedata.Event
import com.viam.feeder.core.network.CoroutinesDispatcherProvider
import com.viam.feeder.core.onSuccess
import com.viam.feeder.data.models.ClockTimer
import com.viam.feeder.data.repository.TimerRepository
import kotlinx.coroutines.launch

class TimerViewModel @ViewModelInject constructor(
    private val timerRepository: TimerRepository,
    private val dispatcherProvider: CoroutinesDispatcherProvider
) : ViewModel() {

    companion object {
        const val TIMER_MODE_SCHEDULING = 0
        const val TIMER_MODE_PERIODIC = 1
        const val MAX_VALUE = 24 * 60
        const val MIN_VALUE = 1
        const val DEFAULT_VALUE = 4 * 60
    }

    private val _timerList = MutableLiveData<Resource<List<ClockTimer>>>()
    val timerList: LiveData<Resource<List<ClockTimer>>> = _timerList

    private val _loading = MutableLiveData<Boolean>()
    val loading: LiveData<Boolean> = _loading

    private val _timerMode = MutableLiveData<Int>()
    val timerMode: LiveData<Int> = _timerMode

    init {
        getTimes()
    }

    private fun getTimes() {
        _loading.postValue(true)
        viewModelScope.launch {
            timerRepository.getTimes().let {
                _timerList.postValue(it)
                _loading.postValue(false)
            }
        }
    }

    private val _openTimerDialog = MutableLiveData<Event<Unit>>()
    val openTimerDialog: LiveData<Event<Unit>> = _openTimerDialog
    fun onRemoveClockTimerClicked(id: Long) {
        _timerList.value.dataOrNull()
            ?.firstOrNull { it.id == id }
            ?.let { timer ->
                removeTimer(timer)
            }
    }

    private fun removeTimer(timer: ClockTimer) {
        _loading.value = true
        viewModelScope.launch(dispatcherProvider.io) {
            timerRepository.removeTime(timer).onSuccess {
                getTimes()
            }.also {
                _loading.postValue(false)
            }
        }
    }


    fun onClickAddClock() {
        _openTimerDialog.postValue(Event(Unit))
    }

    fun onTimeSet(newHour: Int, newMinute: Int) {
        _loading.value = true
        viewModelScope.launch(dispatcherProvider.io) {
            timerRepository.addTime(ClockTimer(hour = newHour, minute = newMinute)).onSuccess {
                getTimes()
            }.also {
                _loading.postValue(false)
            }

        }
    }

    private val _currentValue = MutableLiveData(DEFAULT_VALUE)
    val currentValue: LiveData<Int> = _currentValue

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

}
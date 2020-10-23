package com.viam.feeder.ui.record

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.viam.feeder.core.extensions.timerAsync
import com.viam.feeder.core.livedata.Event
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class RecordViewModel @ViewModelInject constructor() : ViewModel() {

    companion object {
        const val MAX_TIME = 10L
        const val STARTING = 4L
    }

    private lateinit var filePath: String
    private val _recordText = MutableLiveData<String?>()
    val recordText: LiveData<String?> = _recordText

    private val _recordWillStarting = MutableLiveData(STARTING.toString())
    val recordWillStarting: LiveData<String?> = _recordWillStarting

    private val _playClicked = MutableLiveData<Event<Unit>>()
    val playClicked: LiveData<Event<Unit>> = _playClicked

    private val _applyClicked = MutableLiveData<Event<Unit>>()
    val applyClicked: LiveData<Event<Unit>> = _applyClicked

    private val _startRecord = MutableLiveData<Event<Unit>>()
    val startRecord: LiveData<Event<Unit>> = _startRecord

    private val _cancelClicked = MutableLiveData<Event<Unit>>()
    val cancelClicked: LiveData<Event<Unit>> = _cancelClicked

    private val _stopClicked = MutableLiveData<Event<Unit>>()
    val stopClicked: LiveData<Event<Unit>> = _stopClicked

    private var job: Job? = null

    init {

        viewModelScope.launch {
            STARTING.timerAsync {
                if (it == STARTING) {
                    _recordWillStarting.value = null
                    _startRecord.postValue(Event(Unit))
                } else {
                    _recordWillStarting.value = (STARTING - it).toString()
                }
            }
        }
    }

    fun onPlayClicked() {
        _playClicked.value = Event(Unit)
    }

    fun onApplyClicked() {
        _applyClicked.value = Event(Unit)

    }

    fun onCancelClicked() {
        _cancelClicked.value = Event(Unit)
    }

    fun onStopClicked() {
        job?.cancel()
        _recordText.value = null
        _stopClicked.value = Event(Unit)
    }

    private fun onInterval(currentTime: Long) {

        if (currentTime == MAX_TIME) {
            _recordText.value = null
            _stopClicked.value = Event(Unit)
        } else {
            _recordText.value = String.format("00:%02d", MAX_TIME - currentTime)
        }
    }

    fun onRetryClicked() {
        _startRecord.value = Event(Unit)
    }

    fun onStartRecording(filePath: String) {
        this.filePath = filePath
        job?.cancel()
        job = viewModelScope.launch {
            MAX_TIME.timerAsync {
                onInterval(it)
            }
        }
    }


}
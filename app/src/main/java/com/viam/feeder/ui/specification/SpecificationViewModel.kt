package com.viam.feeder.ui.specification

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.viam.feeder.R
import com.viam.feeder.core.network.CoroutinesDispatcherProvider
import com.viam.feeder.core.network.NetworkStatus
import com.viam.feeder.data.repository.GlobalConfigRepository
import com.viam.feeder.models.FeedVolume
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.ticker
import kotlinx.coroutines.launch

class SpecificationViewModel @ViewModelInject constructor(
    private val networkStatus: NetworkStatus,
    private val globalConfigRepository: GlobalConfigRepository,
    private val dispatcherProvider: CoroutinesDispatcherProvider
) :
    ViewModel() {


    private val _feedSounds = MutableLiveData(
        listOf(
            "Cat",
            "Dog",
            "Custom"
        )
    )

    val feedSounds: LiveData<List<String>> = _feedSounds

    private val _feedVolumeList = MutableLiveData(
        listOf(
            FeedVolume(1, 0.33f, R.string.little),
            FeedVolume(2, 0.66f, R.string.medium),
            FeedVolume(3, 1.0f, R.string.large),
        )
    )
    val feedVolumeList: LiveData<List<FeedVolume>> = _feedVolumeList

    private val _ledStates = MutableLiveData(
        listOf(
            "Turn on when feeding",
            "Always turn on",
            "Always turn off"
        )
    )
    val ledStates: LiveData<List<String>> = _ledStates


    fun onFeedVolumeClicked(id: Int) {

        val values = _feedVolumeList.value?.toMutableList()?.map {
            it.selected = id == it.id
            it
        }
        _feedVolumeList.value = values
    }

    private var tickerChannel: ReceiveChannel<Unit>? = null
    private var count: Int = 0

    private val _recordText = MutableLiveData<String>()
    val recordText: LiveData<String> = _recordText

    private val _recordState = MutableLiveData(RECORD_STATE_NOT_RECORDED)
    val recordState: LiveData<Int> = _recordState

    companion object {
        const val RECORD_STATE_RECORDED = 1
        const val RECORD_STATE_NOT_RECORDED = 2
    }

    var job: Job? = null
    fun onRecordClick() {
        job?.cancel()
        if (_recordText.value == null) {
            _recordState.value = RECORD_STATE_NOT_RECORDED
            count = 10
            job = viewModelScope.launch {
                tickerChannel = ticker(delayMillis = 1_000, initialDelayMillis = 0)
                for (event in tickerChannel!!) {
                    if (count == 0) {
                        _recordText.value = null
                        _recordState.value = RECORD_STATE_RECORDED
                        job?.cancel()
                    } else {
                        count--
                        _recordText.value = String.format("00:%02d", count)
                    }
                }
            }
        } else {
            _recordState.value = RECORD_STATE_RECORDED
        }
    }

    override fun onCleared() {
        super.onCleared()
        job?.cancel()
    }

    fun onPlayClicked() {

    }

    fun onRemoveRecordClicked() {
        _recordState.value = RECORD_STATE_NOT_RECORDED
        _recordText.value = null

    }

    private val _currentSoundVolumeValue = MutableLiveData(50f)
    val currentSoundVolumeValue: LiveData<Float> = _currentSoundVolumeValue

    fun onVolumeUpClicked() {
        _currentSoundVolumeValue.value =
            _currentSoundVolumeValue.value?.plus(10)?.coerceAtMost(100f)
    }

    fun onVolumeDownClicked() {
        _currentSoundVolumeValue.value =
            _currentSoundVolumeValue.value?.minus(10)?.coerceAtLeast(0f)

    }

    private val _feedSoundValue = MutableLiveData<Int>()
    val feedSoundValue: LiveData<Int> = _feedSoundValue
    fun onFeedSoundItemClickListener(position: Int) {
        _feedSoundValue.value = position
    }

    fun onLedItemClickListener(position: Int) {

    }

    fun onSoundVolumeChanged(value: Float) {
        _currentSoundVolumeValue.value = value
    }
}
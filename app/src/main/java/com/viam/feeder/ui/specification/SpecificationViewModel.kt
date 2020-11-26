package com.viam.feeder.ui.specification

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.viam.feeder.R
import com.viam.feeder.constants.SETTING_FEED_VOLUME
import com.viam.feeder.constants.SETTING_VOLUME
import com.viam.feeder.core.domain.toLiveTask
import com.viam.feeder.core.livedata.Event
import com.viam.feeder.core.task.compositeTask
import com.viam.feeder.data.domain.ConvertUploadSound
import com.viam.feeder.data.domain.event.SendEvent
import com.viam.feeder.data.models.KeyValue
import com.viam.feeder.models.FeedVolume


class SpecificationViewModel @ViewModelInject constructor(
    convertAndUploadSoundUseCase: ConvertUploadSound,
    sendEvent: SendEvent
) :
    ViewModel() {
    private val _feedSounds = MutableLiveData(
        listOf(
            "From Your Phone",
            "Record",
            "Cat",
            "Dog",
        )
    )
    val feedSounds: LiveData<List<String>> = _feedSounds

    private val _openRecordDialog = MutableLiveData<Event<Unit>>()
    val openRecordDialog: LiveData<Event<Unit>> = _openRecordDialog

    private val _chooseIntentSound = MutableLiveData<Event<Unit>>()
    val chooseIntentSound: LiveData<Event<Unit>> = _chooseIntentSound

    val convertAndUploadSoundUseCaseTask = convertAndUploadSoundUseCase.toLiveTask()

    val feedVolumeEvent = sendEvent.toLiveTask {
        debounce(1000)
    }

    val soundVolumeEvent = sendEvent.toLiveTask {
        debounce(1000)
    }


    val compositeTask = compositeTask(
        convertAndUploadSoundUseCaseTask,
        feedVolumeEvent
    ) {
        cancelable(false)
    }

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
        _feedVolumeList.value?.toMutableList()?.map {
            it.selected = id == it.id
            if (it.selected) {
                feedVolumeEvent.postWithCancel(KeyValue(SETTING_FEED_VOLUME, it.scale * 1000))
            }
            it
        }?.let {
            _feedVolumeList.value = it
        }
    }

    private val _currentSoundVolumeValue = MutableLiveData<Float>()
    val currentSoundVolumeValue: LiveData<Float> = _currentSoundVolumeValue

    fun onVolumeUpClicked() {
        _currentSoundVolumeValue.value =
            _currentSoundVolumeValue.value?.plus(10)?.coerceAtMost(100f)
    }

    fun onVolumeDownClicked() {
        _currentSoundVolumeValue.value =
            _currentSoundVolumeValue.value?.minus(10)?.coerceAtLeast(0f)

    }

    fun onFeedSoundItemClicked(position: Int) {
        if (position == 0) {
            _chooseIntentSound.value = Event(Unit)
        } else if (position == 1) {
            _openRecordDialog.value = Event(Unit)
        }
    }

    fun onLedItemClickListener(position: Int) {

    }

    fun onSoundVolumeChanged(value: Float) {
        _currentSoundVolumeValue.value = value
    }

    fun onRecordFile(input: String, output: String) {
        convertAndUploadSoundUseCaseTask.post(Pair(input, output))
    }

    init {
        soundVolumeEvent.asLiveData().addSource(_currentSoundVolumeValue) {
            soundVolumeEvent.postWithCancel(KeyValue(SETTING_VOLUME))
        }
    }

}
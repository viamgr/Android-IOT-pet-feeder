package com.viam.feeder.ui.specification

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.viam.feeder.R
import com.viam.feeder.constants.SETTING_FEED_DURATION
import com.viam.feeder.constants.SETTING_LED_STATE
import com.viam.feeder.constants.SETTING_SOUND_VOLUME
import com.viam.feeder.core.domain.utils.toLiveTask
import com.viam.feeder.core.livedata.Event
import com.viam.feeder.core.task.compositeTask
import com.viam.feeder.data.domain.event.SendEvent
import com.viam.feeder.data.domain.specification.ConvertUploadSound
import com.viam.feeder.data.domain.specification.UploadSound
import com.viam.feeder.data.models.KeyValue
import com.viam.feeder.models.FeedVolume


class SpecificationViewModel @ViewModelInject constructor(
    convertUploadSound: ConvertUploadSound,
    uploadSound: UploadSound,
    sendEvent: SendEvent
) :
    ViewModel() {
    private val _feedSounds = MutableLiveData(
        listOf(
            "From Your Phone",
            "Record",
            "Piano",
            "Come On",
        )
    )
    val feedSounds: LiveData<List<String>> = _feedSounds

    private val _openRecordDialog = MutableLiveData<Event<Unit>>()
    val openRecordDialog: LiveData<Event<Unit>> = _openRecordDialog

    private val _chooseIntentSound = MutableLiveData<Event<Unit>>()
    val chooseIntentSound: LiveData<Event<Unit>> = _chooseIntentSound

    private val convertAndUploadSoundUseCaseTask = convertUploadSound.toLiveTask()

    private val uploadSoundTask = uploadSound.toLiveTask()

    val feedVolumeEvent = sendEvent.toLiveTask {
        debounce(250)
    }

    val soundVolumeEventTask = sendEvent.toLiveTask()

    val soundTask = compositeTask(
        uploadSoundTask, convertAndUploadSoundUseCaseTask

    )
    val ledStateTask = sendEvent.toLiveTask()

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
                feedVolumeEvent.postWithCancel(KeyValue(SETTING_FEED_DURATION, it.scale * 1000))
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
        when (position) {
            0 -> _chooseIntentSound.value = Event(Unit)
            1 -> _openRecordDialog.value = Event(Unit)
            else -> uploadSoundTask.post(getAudioFileByPosition(position))
        }
    }

    private fun getAudioFileByPosition(position: Int): Int {
        return when (position) {
            2 -> R.raw.piano
            3 -> R.raw.come_on
            else -> R.raw.come_on
        }
    }

    fun onLedItemClickListener(position: Int) {
        ledStateTask.postWithCancel(KeyValue(SETTING_LED_STATE, position))
    }

    fun onSoundVolumeChanged(value: Float) {
        _currentSoundVolumeValue.value = value
    }

    fun onSoundFilePicked(input: String, output: String) {
        convertAndUploadSoundUseCaseTask.post(Pair(input, output))
    }

    init {
        soundVolumeEventTask.asLiveData().addSource(_currentSoundVolumeValue) {
            soundVolumeEventTask.postWithCancel(KeyValue(SETTING_SOUND_VOLUME, it))
        }
    }

}
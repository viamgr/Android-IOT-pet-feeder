package com.viam.feeder.ui.specification

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.viam.feeder.R
import com.viam.feeder.core.domain.utils.toLiveTask
import com.viam.feeder.core.livedata.Event
import com.viam.feeder.core.task.compositeTask
import com.viam.feeder.data.domain.config.*
import com.viam.feeder.data.domain.specification.ConvertUploadSound
import com.viam.feeder.data.domain.specification.UploadSound
import com.viam.feeder.models.FeedVolume
import com.viam.feeder.models.LedState
import com.viam.feeder.models.LedTimer
import com.viam.feeder.models.SoundVolume


class SpecificationViewModel @ViewModelInject constructor(
    convertUploadSound: ConvertUploadSound,
    uploadSound: UploadSound,
    getFeedingDurationVolume: GetFeedingDuration,
    setFeedingDurationVolume: SetFeedingDuration,
    getLedState: GetLedState,
    setLedState: SetLedState,
    getSoundVolume: GetSoundVolume,
    setSoundVolume: SetSoundVolume,
    setLedTurnOffDelay: SetLedTurnOffDelay,
    getLedTurnOffDelay: GetLedTurnOffDelay
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

    private val _feedVolumeList = MutableLiveData(
        listOf(
            FeedVolume(2000, R.string.few),
            FeedVolume(4000, R.string.medium),
            FeedVolume(6000, R.string.much),
        )
    )
    private val _ledTimerList = MutableLiveData(
        listOf(
            60000,
            120000,
            180000,
            240000,
            300000,
            600000,
            1800000,
            3600000,
            4800000,
            86400000
        ).map {
            LedTimer(it, R.string.seconds)
        }
    )

    private val _soundVolumeList = MutableLiveData(
        listOf(
            SoundVolume(0F, R.string.mute),
            SoundVolume(1.3F, R.string.low),
            SoundVolume(2.6F, R.string.medium),
            SoundVolume(3.99F, R.string.high)
        )
    )

    private val _ledStates = MutableLiveData(
        listOf(
            LedState(0, R.string.turn_on_feeding),
            LedState(1, R.string.always_on),
            LedState(2, R.string.always_off)
        )
    )

    val feedSounds: LiveData<List<String>> = _feedSounds

    private val _openRecordDialog = MutableLiveData<Event<Unit>>()
    val openRecordDialog: LiveData<Event<Unit>> = _openRecordDialog

    private val _chooseIntentSound = MutableLiveData<Event<Unit>>()
    val chooseIntentSound: LiveData<Event<Unit>> = _chooseIntentSound

    private val convertAndUploadSoundUseCaseTask = convertUploadSound.toLiveTask()

    private val uploadSoundTask = uploadSound.toLiveTask()

    private val setSoundVolumeEventTask = setSoundVolume.toLiveTask {
        debounce(2000)
    }

    private val _feedingVolumeValue = MutableLiveData(R.string.choose)
    val feedingVolumeValue: LiveData<Int> = _feedingVolumeValue

    private val _soundVolumeValue = MutableLiveData(R.string.choose)
    val soundVolumeValue: LiveData<Int> = _soundVolumeValue

    private val _ledTimerValue = MutableLiveData<LedTimer>()
    val ledTimerValue: LiveData<LedTimer>
        get() = _ledTimerValue

    private val _ledStateValue = MutableLiveData(R.string.choose)
    val ledStateValue: LiveData<Int> = _ledStateValue

    private val getSoundVolumeEventTask = getSoundVolume.toLiveTask().also { liveTask ->
        liveTask.post(Unit)
    }.onSuccess { volume ->
        volume?.let { value: Float ->
            _soundVolumeList.value?.firstOrNull { volume -> volume.value == value }?.let {
                _soundVolumeValue.value = it.label
            }
        }
    }

    private val setFeedingDurationVolumeTask = setFeedingDurationVolume.toLiveTask()
    private val getFeedingVolumeEventTask = getFeedingDurationVolume.toLiveTask().also {
        it.post(Unit)
    }.onSuccess { duration ->
        duration?.let { value: Int ->
            _feedVolumeList.value?.firstOrNull { volume -> volume.duration == value }?.let {
                _feedingVolumeValue.value = it.label
            }
        }
    }

    private val setLedStateTask = setLedState.toLiveTask()
    private val getLedStateTask = getLedState.toLiveTask().also {
        it.post(Unit)
    }.onSuccess { ledState ->
        ledState?.let { value: Int ->
            _ledStates.value?.firstOrNull { state -> state.value == value }?.let {
                _ledStateValue.value = it.label
            }
        }
    }

    private val setLedTurnOffDelayTask = setLedTurnOffDelay.toLiveTask()
    private val getLedTurnOffDelayTask = getLedTurnOffDelay.toLiveTask().onSuccess { duration ->
        duration?.let { value: Int ->
            _ledTimerList.value?.firstOrNull { ledTimer -> ledTimer.value == value }?.let {
                _ledTimerValue.value = it
            }
        }
    }.also {
        it.post(Unit)
    }

    val tasks = compositeTask(
        setLedStateTask, getLedStateTask,
        getLedTurnOffDelayTask, setLedTurnOffDelayTask,
        getFeedingVolumeEventTask,
        setFeedingDurationVolumeTask,
        setSoundVolumeEventTask,
        getSoundVolumeEventTask,
        uploadSoundTask,
        convertAndUploadSoundUseCaseTask
    )

    val feedVolumeList: LiveData<List<FeedVolume>> = _feedVolumeList
    val soundVolumeList: LiveData<List<SoundVolume>> = _soundVolumeList
    val ledStates: LiveData<List<LedState>> = _ledStates
    val ledTimerList: LiveData<List<LedTimer>> = _ledTimerList

    fun onFeedingVolumeClicked(position: Int) {
        val value = _feedVolumeList.value!![position].duration
        setFeedingDurationVolumeTask.postWithCancel(value)
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
        setLedStateTask.postWithCancel(position)
    }

    fun onSoundVolumeChanged(value: Int) {
        setSoundVolumeEventTask.postWithCancel(_soundVolumeList.value!![value].value)
    }

    fun onSoundFilePicked(input: String, output: String) {
        convertAndUploadSoundUseCaseTask.post(Pair(input, output))
    }

    fun onLedTimerItemClickListener(position: Int) {
        setLedTurnOffDelayTask.postWithCancel(_ledTimerList.value!![position].value)
    }
}
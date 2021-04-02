package com.viam.feeder.ui.dashboard

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.map
import com.viam.feeder.R
import com.viam.feeder.constants.EVENT_COMPOSITE_FEEDING
import com.viam.feeder.constants.EVENT_FEEDING
import com.viam.feeder.constants.EVENT_LED_TIMER
import com.viam.feeder.constants.EVENT_PLAY_FEEDING_AUDIO
import com.viam.feeder.core.domain.utils.toLiveTask
import com.viam.feeder.core.livedata.Event
import com.viam.feeder.core.utility.launchInScope
import com.viam.feeder.data.domain.config.*
import com.viam.feeder.data.domain.event.SendEvent
import com.viam.feeder.data.domain.specification.ConvertUploadSound
import com.viam.feeder.data.domain.specification.UploadSound
import com.viam.feeder.models.FeedVolume
import com.viam.feeder.models.LedState
import com.viam.feeder.models.LedTimer
import com.viam.feeder.models.SoundVolume

class DashboardViewModel @ViewModelInject constructor(
    sendEvent: SendEvent,
    convertUploadSound: ConvertUploadSound,
    uploadSound: UploadSound,
    getFeedingDurationVolume: GetFeedingDuration,
    private val setFeedingDurationVolume: SetFeedingDuration,
    getLedState: GetLedState,
    private val setLedState: SetLedState,
    getSoundVolume: GetSoundVolume,
    private val setSoundVolume: SetSoundVolume,
    private val setLedTurnOffDelay: SetLedTurnOffDelay,
    getLedTurnOffDelay: GetLedTurnOffDelay
) : ViewModel() {
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
            1 * 3600000,
            2 * 3600000,
            3 * 3600000,
            4 * 3600000,
            24 * 3600000
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

    val compositeSendEvent = sendEvent.toLiveTask()
    val ledSendEvent = sendEvent.toLiveTask()
    val callingSendEvent = sendEvent.toLiveTask()
    val feedingSendEvent = sendEvent.toLiveTask()

    val soundVolumeValue: LiveData<Int> = getSoundVolume().map {
        _soundVolumeList.value?.firstOrNull { volume -> volume.value == it }?.label
            ?: R.string.choose
    }

    val feedingVolumeValue: LiveData<Int> = getFeedingDurationVolume().map {
        _feedVolumeList.value?.firstOrNull { item -> item.duration == it }?.label
            ?: R.string.choose
    }

    val ledStateValue: LiveData<Int> = getLedState().map {
        _ledStates.value?.firstOrNull { item -> item.value == it }?.label
            ?: R.string.choose
    }

    val ledTimerValue: LiveData<LedTimer?> = getLedState().map {
        _ledTimerList.value?.firstOrNull { item -> item.value == it }
    }

    val feedVolumeList: LiveData<List<FeedVolume>> = _feedVolumeList
    val soundVolumeList: LiveData<List<SoundVolume>> = _soundVolumeList
    val ledStates: LiveData<List<LedState>> = _ledStates
    val ledTimerList: LiveData<List<LedTimer>> = _ledTimerList

    fun onFeedingVolumeClicked(position: Int) = launchInScope {
        setFeedingDurationVolume(_feedVolumeList.value!![position].duration)
    }

    fun onFeedSoundItemClicked(position: Int) {
        when (position) {
            0 -> _chooseIntentSound.value = Event(Unit)
            1 -> _openRecordDialog.value = Event(Unit)
            else -> uploadSoundTask.execute(getAudioFileByPosition(position))
        }
    }

    private fun getAudioFileByPosition(position: Int): Int {
        return when (position) {
            2 -> R.raw.piano
            3 -> R.raw.come_on
            else -> R.raw.come_on
        }
    }

    fun onLedItemClickListener(position: Int) = launchInScope {
        setLedState(position)
    }


    fun onSoundVolumeChanged(value: Int) = launchInScope {
        setSoundVolume.invoke(_soundVolumeList.value!![value].value)
    }

    fun onSoundFilePicked(input: String, output: String) {
        convertAndUploadSoundUseCaseTask.execute(Pair(input, output))
    }

    fun onLedTimerItemClickListener(position: Int) = launchInScope {
        setLedTurnOffDelay(_ledTimerList.value!![position].value)
    }

    fun sendCompositeFeedingEvent() {
        compositeSendEvent.execute(EVENT_COMPOSITE_FEEDING)
    }

    fun sendLightEvent() {
        ledSendEvent.execute(EVENT_LED_TIMER)
    }

    fun sendFeedingEvent() {
        feedingSendEvent.execute(EVENT_FEEDING)
    }

    fun sendCallingEvent() {
        callingSendEvent.execute(EVENT_PLAY_FEEDING_AUDIO)
    }
}
package com.viam.feeder.ui.dashboard

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.map
import com.part.livetaskcore.livatask.combine
import com.part.livetaskcore.usecases.asLiveTask
import com.viam.feeder.R
import com.viam.feeder.core.livedata.Event
import com.viam.feeder.core.utility.launchInScope
import com.viam.feeder.data.constants.EVENT_COMPOSITE_FEEDING
import com.viam.feeder.data.constants.EVENT_FEEDING
import com.viam.feeder.data.constants.EVENT_LED_TIMER
import com.viam.feeder.data.constants.EVENT_PLAY_FEEDING_AUDIO
import com.viam.feeder.domain.usecase.config.*
import com.viam.feeder.domain.usecase.event.SendEvent
import com.viam.feeder.domain.usecase.specification.ConvertUploadSound
import com.viam.feeder.models.FeedVolume
import com.viam.feeder.models.LedTimer
import com.viam.feeder.models.SoundVolume
import dagger.hilt.android.lifecycle.HiltViewModel
import java.io.InputStream
import javax.inject.Inject

@HiltViewModel
class DashboardViewModel @Inject constructor(
    getFeedingDurationVolume: GetFeedingDuration,
    getSoundVolume: GetSoundVolume,
    getLedTurnOffDelay: GetLedTurnOffDelay,
    convertUploadSound: ConvertUploadSound,
    uploadBinary: UploadBinary,
    setFeedingDuration: SetFeedingDuration,
    setSoundVolume: SetSoundVolume,
    sendEvent: SendEvent,
    setLedTurnOffDelay: SetLedTurnOffDelay
) : ViewModel() {

    private val uploadBinaryTask = uploadBinary.asLiveTask()
    private val feedingDurationTask = setFeedingDuration.asLiveTask()
    private val soundLiveTask = setSoundVolume.asLiveTask()
    private val convertUploadSoundTask = convertUploadSound.asLiveTask()
    private val setLedTurnOffDelayTask = setLedTurnOffDelay.asLiveTask()
    private val sendEventTask = sendEvent.asLiveTask()

    val combinedTasks = combine(
        uploadBinaryTask,
        feedingDurationTask,
        soundLiveTask,
        convertUploadSoundTask,
        setLedTurnOffDelayTask,
        sendEventTask,
    )

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

    val feedSounds: LiveData<List<String>> = _feedSounds

    private val _openRecordDialog = MutableLiveData<Event<Unit>>()
    val openRecordDialog: LiveData<Event<Unit>> = _openRecordDialog

    private val _requestSoundInputStream = MutableLiveData<Event<Int>>()
    val requestInputStreamOfRaw: LiveData<Event<Int>> = _requestSoundInputStream

    private val _chooseIntentSound = MutableLiveData<Event<Unit>>()
    val chooseIntentSound: LiveData<Event<Unit>> = _chooseIntentSound

    val soundVolumeValue: LiveData<Int> = getSoundVolume().map {
        _soundVolumeList.value?.firstOrNull { volume -> volume.value == it }?.label
            ?: R.string.choose
    }

    val feedingVolumeValue: LiveData<Int> = getFeedingDurationVolume().map {
        _feedVolumeList.value?.firstOrNull { item -> item.duration == it }?.label
            ?: R.string.choose
    }

    val ledTimerValue: LiveData<LedTimer?> = getLedTurnOffDelay().map {
        _ledTimerList.value?.firstOrNull { item -> item.value == it }
    }

    val feedVolumeList: LiveData<List<FeedVolume>> = _feedVolumeList
    val soundVolumeList: LiveData<List<SoundVolume>> = _soundVolumeList
    val ledTimerList: LiveData<List<LedTimer>> = _ledTimerList


    fun onFeedingVolumeClicked(position: Int) = launchInScope {
        feedingDurationTask(_feedVolumeList.value!![position].duration)
    }

    fun onFeedSoundItemClicked(position: Int) {
        when (position) {
            0 -> _chooseIntentSound.value = Event(Unit)
            1 -> _openRecordDialog.value = Event(Unit)
            else -> _requestSoundInputStream.value = Event(getAudioFileByPosition(position))
        }
    }

    fun onGetInputStream(inputStream: InputStream) = launchInScope {
        uploadBinaryTask(UploadBinary.UploadBinaryParams(FEEDING_SOUND, inputStream))
    }

    private fun getAudioFileByPosition(position: Int): Int {
        return when (position) {
            2 -> R.raw.piano
            3 -> R.raw.come_on
            else -> R.raw.come_on
        }
    }

    fun onSoundVolumeChanged(value: Int) = launchInScope {
        soundLiveTask(_soundVolumeList.value!![value].value)
    }


    fun onSoundFilePicked(filePath: String) = launchInScope {
        convertUploadSoundTask(
            ConvertUploadSound.ConvertUploadSoundParams(FEEDING_SOUND, filePath)
        )
    }

    fun onLedTimerItemClickListener(position: Int) = launchInScope {
        setLedTurnOffDelayTask(_ledTimerList.value!![position].value)
    }

    fun sendCompositeFeedingEvent() = launchInScope {
        sendEventTask(EVENT_COMPOSITE_FEEDING)
    }

    fun sendLightEvent() = launchInScope {
        sendEventTask(EVENT_LED_TIMER)
    }

    fun sendFeedingEvent() = launchInScope {
        sendEventTask(EVENT_FEEDING)
    }

    fun sendCallingEvent() = launchInScope {
        sendEventTask(EVENT_PLAY_FEEDING_AUDIO)
    }

    companion object {
        const val FEEDING_SOUND = "feeding.mp3"
    }
}
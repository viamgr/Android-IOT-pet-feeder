package com.viam.feeder.ui.dashboard

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.map
import com.part.livetaskcore.usecases.asLiveTask
import com.viam.feeder.R
import com.viam.feeder.constants.EVENT_COMPOSITE_FEEDING
import com.viam.feeder.constants.EVENT_FEEDING
import com.viam.feeder.constants.EVENT_LED_TIMER
import com.viam.feeder.constants.EVENT_PLAY_FEEDING_AUDIO
import com.viam.feeder.core.livedata.Event
import com.viam.feeder.core.utility.launchInScope
import com.viam.feeder.data.domain.config.*
import com.viam.feeder.data.domain.event.SendEvent
import com.viam.feeder.data.domain.specification.ConvertUploadSound
import com.viam.feeder.models.FeedVolume
import com.viam.feeder.models.LedTimer
import com.viam.feeder.models.SoundVolume
import kotlinx.coroutines.flow.collect
import java.io.InputStream

class DashboardViewModel @ViewModelInject constructor(
    getFeedingDurationVolume: GetFeedingDuration,
    getSoundVolume: GetSoundVolume,
    getLedTurnOffDelay: GetLedTurnOffDelay,
    private val convertUploadSound: ConvertUploadSound,
    private val uploadBinary: UploadBinary,
    private val sendEvent: SendEvent,
    private val setFeedingDuration: SetFeedingDuration,
    private val setSoundVolume: SetSoundVolume,
    private val setLedTurnOffDelay: SetLedTurnOffDelay
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
    val feedingDurationTask = setFeedingDuration.asLiveTask {
        onLoading {
            println(it)
        }
        onSuccess {
            println(it)
        }
        onError {
            it.printStackTrace()
            println(it)
        }
    }

    fun onFeedingVolumeClicked(position: Int) = launchInScope {
        feedingDurationTask.run(_feedVolumeList.value!![position].duration)
//        setFeedingDurationVolume(_feedVolumeList.value!![position].duration).collect()
    }

    fun onFeedSoundItemClicked(position: Int) {
        when (position) {
            0 -> _chooseIntentSound.value = Event(Unit)
            1 -> _openRecordDialog.value = Event(Unit)
            else -> _requestSoundInputStream.value = Event(getAudioFileByPosition(position))
        }
    }

    fun onGetInputStream(inputStream: InputStream) = launchInScope {
        uploadBinary(UploadBinary.UploadBinaryParams(FEEDING_SOUND, inputStream)).collect()
    }

    private fun getAudioFileByPosition(position: Int): Int {
        return when (position) {
            2 -> R.raw.piano
            3 -> R.raw.come_on
            else -> R.raw.come_on
        }
    }

    val soundLiveTask = setSoundVolume.asLiveTask()
    fun onSoundVolumeChanged(value: Int) = launchInScope {
        soundLiveTask.run(_soundVolumeList.value!![value].value)
    }

    fun onSoundFilePicked(filePath: String) = launchInScope {
        convertUploadSound(
            ConvertUploadSound.ConvertUploadSoundParams(
                FEEDING_SOUND,
                filePath
            )
        ).collect()
    }

    fun onLedTimerItemClickListener(position: Int) = launchInScope {
        setLedTurnOffDelay(_ledTimerList.value!![position].value)
    }

    fun sendCompositeFeedingEvent() = launchInScope {
        sendEvent(EVENT_COMPOSITE_FEEDING)
    }

    fun sendLightEvent() = launchInScope {
        sendEvent(EVENT_LED_TIMER)
    }

    fun sendFeedingEvent() = launchInScope {
        sendEvent(EVENT_FEEDING)
    }

    fun sendCallingEvent() = launchInScope {
        sendEvent(EVENT_PLAY_FEEDING_AUDIO)
    }

    companion object {
        const val FEEDING_SOUND = "feeding.mp3"
    }
}
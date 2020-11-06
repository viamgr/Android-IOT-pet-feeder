package com.viam.feeder.ui.specification

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.*
import com.viam.feeder.R
import com.viam.feeder.core.Resource
import com.viam.feeder.core.livedata.Event
import com.viam.feeder.core.task.LiveTask
import com.viam.feeder.core.task.compositeTask
import com.viam.feeder.core.task.livaTask
import com.viam.feeder.data.domain.ConvertAndUploadSoundUseCase
import com.viam.feeder.models.FeedVolume
import com.viam.feeder.ui.wifi.ConnectionUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class SpecificationViewModel @ViewModelInject constructor(
    private val convertAndUploadSoundUseCase: ConvertAndUploadSoundUseCase
) :
    ViewModel() {
    val connectionStatus = ConnectionUtil.connectionState
    private val _feedSounds = MutableLiveData(
        listOf(
            "Cat",
            "Dog",
            "From Your Phone",
            "Record"
        )
    )
    val feedSounds: LiveData<List<String>> = _feedSounds

    val a = liveData<String> {
        withContext(Dispatchers.IO) {

            repeat(1000) {
                emit("$it")
                println("work on $it")

                delay(1000)
            }
        }
    }

    private val _openRecordDialog = MutableLiveData<Event<Unit>>()
    val openRecordDialog: LiveData<Event<Unit>> = _openRecordDialog

    private val _chooseIntentSound = MutableLiveData<Event<Unit>>()
    val chooseIntentSound: LiveData<Event<Unit>> = _chooseIntentSound

    val convertAndUploadSoundRequest: LiveTask<Pair<String, String>, Unit> =
        livaTask { params ->
            emit(convertAndUploadSoundUseCase(params))
        }


    val convertAndUploadSoundRequest2: LiveTask<String, Unit> = livaTask {
        emit(Resource.Loading)
        delay(50)
        emit(Resource.Success(Unit))
        delay(50)
        emit(Resource.Loading)
        delay(50)
        emit(Resource.Success(Unit))
        delay(50)
        emit(Resource.Error(Exception("Custom Error")))
    }

    init {
        viewModelScope.launch {
            repeat(1000) {

                delay(300)
                convertAndUploadSoundRequest2.retry()
            }
        }
    }

    val compositeTask: LiveTask<Any, Any> = compositeTask(
        convertAndUploadSoundRequest,
        convertAndUploadSoundRequest2
    )
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
        values?.let {
            _feedVolumeList.value = it
        }
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

    fun onFeedSoundItemClicked(position: Int) {
        if (position + 1 == _feedSounds.value?.size) {
            _openRecordDialog.value = Event(Unit)
        } else if (position + 2 == _feedSounds.value?.size) {
            _chooseIntentSound.value = Event(Unit)
        }
    }

    fun onLedItemClickListener(position: Int) {

    }

    fun onSoundVolumeChanged(value: Float) {
        _currentSoundVolumeValue.value = value
    }

    fun onRecordFile(input: String, output: String) {
        convertAndUploadSoundRequest.execute(Pair(input, output))
//        convertAndUploadSoundRequest2.execute("Test")
    }

    init {
        convertAndUploadSoundRequest2.execute("asdasd")

    }
}
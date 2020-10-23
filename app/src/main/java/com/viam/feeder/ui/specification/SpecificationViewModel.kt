package com.viam.feeder.ui.specification

import android.os.ParcelFileDescriptor
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.viam.feeder.R
import com.viam.feeder.core.livedata.Event
import com.viam.feeder.core.network.CoroutinesDispatcherProvider
import com.viam.feeder.data.repository.UploadRepository
import com.viam.feeder.models.FeedVolume
import kotlinx.coroutines.launch
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.internal.Util
import okio.BufferedSink
import okio.Okio
import okio.Source
import java.io.File
import java.io.IOException
import java.io.InputStream


class SpecificationViewModel @ViewModelInject constructor(
    private val uploadRepository: UploadRepository,
    private val coroutinesDispatcherProvider: CoroutinesDispatcherProvider
) :
    ViewModel() {


    private val _feedSounds = MutableLiveData(
        listOf(
            "Cat",
            "Dog",
            "From Your Phone",
            "Record"
        )
    )
    val feedSounds: LiveData<List<String>> = _feedSounds

    private val _openRecordDialog = MutableLiveData<Event<Unit>>()
    val openRecordDialog: LiveData<Event<Unit>> = _openRecordDialog

    private val _chooseIntentSound = MutableLiveData<Event<Unit>>()
    val chooseIntentSound: LiveData<Event<Unit>> = _chooseIntentSound


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

    private val _loading = MutableLiveData<Boolean>()
    val loading: LiveData<Boolean> = _loading
    fun onRecordFile(path: String) {
        _loading.value = true
        viewModelScope.launch(coroutinesDispatcherProvider.io) {
            val file = File(path)
            val requestFile: RequestBody =
                RequestBody.create(MediaType.parse("multipart/form-data"), file)
            val body = MultipartBody.Part.createFormData("filename", file.name, requestFile)

            uploadRepository.uploadEating(body)
            _loading.postValue(false)
        }

    }

    fun createBody(contentType: MediaType, fd: ParcelFileDescriptor): RequestBody {
        return object : RequestBody() {
            override fun contentType(): MediaType? {
                return contentType
            }

            override fun contentLength(): Long {
                return fd.statSize
            }

            @Throws(IOException::class)
            override fun writeTo(sink: BufferedSink) {
                var source: Source? = null
                try {
                    source = Okio.source(ParcelFileDescriptor.AutoCloseInputStream(fd))
                    sink.writeAll(source)
                } finally {
                    Util.closeQuietly(source)
                }
            }
        }
    }

    fun onFile(inputStream: InputStream) {

        _loading.value = true
        viewModelScope.launch(coroutinesDispatcherProvider.io) {

            val part = MultipartBody.Part.createFormData(
                "filename", "filename.mp3", RequestBody.create(
                    MediaType.parse("audio/mpeg"),
                    inputStream.readBytes()
                )
            )

            val resource = uploadRepository.uploadEating(part)
            _loading.postValue(false)

        }
    }
}
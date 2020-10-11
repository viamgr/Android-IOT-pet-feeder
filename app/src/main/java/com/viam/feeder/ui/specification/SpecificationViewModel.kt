package com.viam.feeder.ui.specification

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.viam.feeder.R
import com.viam.feeder.core.network.CoroutinesDispatcherProvider
import com.viam.feeder.core.network.NetworkStatus
import com.viam.feeder.models.FeedVolume
import com.viam.feeder.services.GlobalConfigRepository

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

    private val _feedVolume = MutableLiveData(
        listOf(
            FeedVolume(1, 0.33f, R.string.little, R.color.green_500),
            FeedVolume(1, 0.66f, R.string.medium, R.color.grey_500),
            FeedVolume(1, 1.0f, R.string.large, R.color.green_500),
        )
    )
    val feedVolume: LiveData<List<FeedVolume>> = _feedVolume

    private val _ledStates = MutableLiveData(
        listOf(
            "Turn on when feeding",
            "Always turn on",
            "Always turn off"
        )
    )
    val ledStates: LiveData<List<String>> = _ledStates


    fun onFeedVolumeClicked(id: Int) {

        val values = _feedVolume.value
        values
            ?.map {
                it.tintColor = R.color.grey_500
                it
            }
            ?.first { it.id == id }
            ?.let {
                it.tintColor = R.color.green_500
            }
        _feedVolume.value = values
    }
}
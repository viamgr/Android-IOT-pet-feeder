package com.viam.feeder.ui.wifi

import androidx.hilt.Assisted
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.viam.feeder.core.livedata.Event
import java.util.concurrent.atomic.AtomicBoolean

class WifiViewModel @ViewModelInject constructor(
    @Assisted savedState: SavedStateHandle,
) : ViewModel() {
    private val _enableWifiClicked = MutableLiveData<Event<Unit>>()
    val enableWifiClicked: LiveData<Event<Unit>> = _enableWifiClicked
    val allowUnknownWifi = savedState.get<Boolean>("allowUnknownWifi") ?: true
    var ignoredInitialValue = AtomicBoolean(false)

    fun onEnableWifi() {
        _enableWifiClicked.value = Event(Unit)
    }

}
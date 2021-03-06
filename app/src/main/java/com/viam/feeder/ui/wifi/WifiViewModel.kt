package com.viam.feeder.ui.wifi

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.viam.feeder.core.livedata.Event
import dagger.hilt.android.lifecycle.HiltViewModel
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject

@HiltViewModel
class WifiViewModel @Inject constructor(
    savedState: SavedStateHandle,
) : ViewModel() {
    private val _enableWifiClicked = MutableLiveData<Event<Unit>>()
    val enableWifiClicked: LiveData<Event<Unit>> = _enableWifiClicked
    val allowUnknownWifi = savedState.get<Boolean>("allowUnknownWifi") ?: true
    var ignoredInitialValue = AtomicBoolean(false)

    fun onEnableWifi() {
        _enableWifiClicked.value = Event(Unit)
    }

}
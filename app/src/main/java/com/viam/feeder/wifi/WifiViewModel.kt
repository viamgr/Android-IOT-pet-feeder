package com.viam.feeder.wifi

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.viam.feeder.livedata.Event

class WifiViewModel : ViewModel() {
    companion object {
        const val CURRENT_STATUS_DISABLED = -1
        const val CURRENT_STATUS_CONNECTING = 0
        const val CURRENT_STATUS_DONE = 1
        const val CURRENT_STATUS_RETRY = 2
    }

    private val _wifiState = MutableLiveData<Int>(CURRENT_STATUS_CONNECTING)
    val wifiState: LiveData<Int> = _wifiState
    private val _enableWifiClicked = MutableLiveData<Event<Unit>>()
    val enableWifiClicked: LiveData<Event<Unit>> = _enableWifiClicked

    private val _retry = MutableLiveData<Event<Unit>>(Event(Unit))
    val retry: LiveData<Event<Unit>> = _retry

    fun retry() {
        _retry.value = Event(Unit)

    }

    fun setWifiState(wifiEnabled: Int) {
        if (_wifiState.value != wifiEnabled)
            _wifiState.value = wifiEnabled
    }

    fun onEnableWifi() {
        _enableWifiClicked.value = Event(Unit)
    }

}
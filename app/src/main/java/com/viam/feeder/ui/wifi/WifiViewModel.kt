package com.viam.feeder.ui.wifi

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.viam.feeder.core.network.NetworkStatus
import com.viam.feeder.livedata.Event

class WifiViewModel @ViewModelInject constructor(val networkStatus: NetworkStatus) : ViewModel() {

    private val _enableWifiClicked = MutableLiveData<Event<Unit>>()
    val enableWifiClicked: LiveData<Event<Unit>> = _enableWifiClicked

    fun onEnableWifi() {
        _enableWifiClicked.value = Event(Unit)
    }


}
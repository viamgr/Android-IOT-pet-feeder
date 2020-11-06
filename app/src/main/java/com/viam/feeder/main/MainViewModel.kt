package com.viam.feeder.main

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.ViewModel
import com.viam.feeder.ui.wifi.ConnectionUtil

class MainViewModel @ViewModelInject constructor() : ViewModel() {
    val connectionStatus = ConnectionUtil.connectionState

    var isWifiDialogShowing: Boolean = false
}

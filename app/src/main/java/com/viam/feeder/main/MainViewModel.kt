package com.viam.feeder.main

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.ViewModel
import java.util.concurrent.atomic.AtomicBoolean

class MainViewModel @ViewModelInject constructor() : ViewModel() {
    var askedWifiPermissions = AtomicBoolean(false)
    var isWifiDialogShowing: Boolean = false
}

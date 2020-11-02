package com.viam.feeder.main

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.ViewModel
import com.viam.feeder.core.network.NetworkStatus

class MainViewModel @ViewModelInject constructor(val networkStatus: NetworkStatus) : ViewModel() {

}

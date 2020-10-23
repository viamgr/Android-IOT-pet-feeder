package com.viam.feeder.main

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.viam.feeder.core.network.NetworkStatus
import kotlinx.coroutines.launch

class MainViewModel @ViewModelInject constructor(
    val networkStatus: NetworkStatus
) : ViewModel() {

    init {
        viewModelScope.launch {
//            networkStatus.check()
        }
    }
}

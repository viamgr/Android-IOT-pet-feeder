package com.viam.feeder.main

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.viam.feeder.core.network.NetworkStatus
import kotlinx.coroutines.launch

class MainViewModel @ViewModelInject constructor(
    private val networkStatus: NetworkStatus
) : ViewModel() {
    fun test() {


    }

    init {
        viewModelScope.launch {
            networkStatus.check()
        }
    }
}

package com.viam.feeder.ui.setting

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.ViewModel
import com.viam.feeder.core.network.CoroutinesDispatcherProvider
import com.viam.feeder.core.network.NetworkStatus
import com.viam.feeder.data.repository.GlobalConfigRepository

class SettingViewModel @ViewModelInject constructor(
    private val networkStatus: NetworkStatus,
    private val globalConfigRepository: GlobalConfigRepository,
    private val dispatcherProvider: CoroutinesDispatcherProvider
) :
    ViewModel() {


}
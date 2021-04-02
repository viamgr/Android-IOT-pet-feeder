package com.viam.feeder.main

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import com.viam.feeder.data.domain.config.GetConfig
import com.viam.feeder.socket.WebSocketApi
import java.util.concurrent.atomic.AtomicBoolean

class MainViewModel @ViewModelInject constructor(
    getConfig: GetConfig,
    webSocketApi: WebSocketApi
) : ViewModel() {
    init {
        webSocketApi.openWebSocket()
    }

    var askedWifiPermissions = AtomicBoolean(false)
    var isWifiDialogShowing: Boolean = false
    val downloadConfigProgress = getConfig().asLiveData()
    val uploadFileProgress = webSocketApi.progress.asLiveData()
}

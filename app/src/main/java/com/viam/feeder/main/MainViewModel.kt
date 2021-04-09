package com.viam.feeder.main

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import com.part.livetaskcore.usecases.asLiveTask
import com.viam.feeder.core.utility.launchInScope
import com.viam.feeder.data.domain.config.GetConfig
import com.viam.websocket.WebSocketApi
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
    val getConfigTask = getConfig.asLiveTask {
        cancelable(false)
    }.also {
        launchInScope {
            it.invoke(Unit)
        }
    }


    val transferFileProgress = webSocketApi.progress.asLiveData()
}

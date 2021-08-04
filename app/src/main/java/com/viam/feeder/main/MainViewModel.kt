package com.viam.feeder.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import com.part.livetaskcore.usecases.asLiveTask
import com.viam.feeder.core.utility.launchInScope
import com.viam.feeder.data.domain.config.GetConfig
import com.viam.websocket.WebSocketApi
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
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
            delay(1000)
            it.invoke(Unit)

            delay(2000)

        }
    }


    val transferFileProgress = webSocketApi.progress.asLiveData()
}


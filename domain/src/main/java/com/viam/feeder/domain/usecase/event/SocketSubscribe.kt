package com.viam.feeder.domain.usecase.event

import com.viam.feeder.domain.base.CoroutinesDispatcherProvider
import com.viam.feeder.domain.base.FlowUseCase
import com.viam.feeder.domain.repositories.socket.WebSocketRepository
import com.viam.feeder.domain.repositories.system.JsonPreferences
import com.viam.resource.Resource
import com.viam.resource.Resource.Loading
import com.viam.websocket.model.SocketConnectionStatus
import com.viam.websocket.model.SocketConnectionStatus.Configured
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.json.JSONObject
import java.io.File
import javax.inject.Inject
import javax.inject.Named

@OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
class SocketSubscribe @Inject constructor(
    coroutinesDispatcherProvider: CoroutinesDispatcherProvider,
    private val webSocketRepository: WebSocketRepository,
    private val jsonPreferences: JsonPreferences,
    @Named("configFile") private val configFile: File,
) : FlowUseCase<Unit, SocketConnectionStatus>(coroutinesDispatcherProvider.io) {

    override fun execute(parameter: Unit): Flow<Resource<SocketConnectionStatus>> {
        return webSocketRepository.subscribeAndPair(configFile.outputStream()).map {
            when (it) {
                is Configured -> Resource.Success(it).also {
                    savePref()
                }
                is SocketConnectionStatus.Failure -> Resource.Error(it.exception)
                else -> Loading(it)
            }
        }
    }

    private fun savePref() {
        configFile.readText().let {
            jsonPreferences.storeJson(JSONObject(it))
        }
    }
}
package com.viam.feeder.domain.usecase.config

import com.viam.feeder.domain.base.CoroutinesDispatcherProvider
import com.viam.feeder.domain.base.FlowUseCase
import com.viam.feeder.domain.base.toResource
import com.viam.feeder.domain.repositories.socket.WebSocketRepository
import com.viam.feeder.domain.repositories.system.JsonPreferences
import com.viam.feeder.shared.FeederConstants.Companion.CONFIG_FILE_PATH
import com.viam.resource.Resource
import com.viam.resource.onSuccess
import com.viam.websocket.model.SocketTransfer
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.File
import javax.inject.Inject
import javax.inject.Named


class GetConfig @Inject constructor(
    private val coroutinesDispatcherProvider: CoroutinesDispatcherProvider,
    @Named("configFile") private val configFile: File,
    private val jsonPreferences: JsonPreferences,
    private val webSocketRepository: WebSocketRepository,
) : FlowUseCase<Unit, SocketTransfer>(coroutinesDispatcherProvider.io) {

    override fun execute(parameter: Unit): Flow<Resource<SocketTransfer>> {
        return webSocketRepository
            .download("/$CONFIG_FILE_PATH", configFile)
            .map { socketTransfer ->
                withContext(coroutinesDispatcherProvider.main) {
                    socketTransfer.toResource().onSuccess {
                        configFile.readText().let {
                            jsonPreferences.storeJson(JSONObject(it))
                        }
                    }
                }
            }
    }

}
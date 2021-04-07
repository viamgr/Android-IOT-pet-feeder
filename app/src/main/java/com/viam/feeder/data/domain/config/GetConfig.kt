package com.viam.feeder.data.domain.config

import com.part.livetaskcore.usecases.FlowUseCase
import com.viam.feeder.core.network.CoroutinesDispatcherProvider
import com.viam.feeder.core.utility.toResource
import com.viam.feeder.data.storage.JsonPreferences
import com.viam.feeder.di.AppModule.Companion.configFilePath
import com.viam.resource.Resource
import com.viam.resource.onSuccess
import com.viam.websocket.WebSocketApi
import com.viam.websocket.model.SocketTransfer
import dagger.hilt.android.scopes.ActivityScoped
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.json.JSONObject
import java.io.File
import javax.inject.Inject
import javax.inject.Named

@ActivityScoped
class GetConfig @Inject constructor(
    coroutinesDispatcherProvider: CoroutinesDispatcherProvider,
    @Named("configFile") private val configFile: File,
    private val jsonPreferences: JsonPreferences,
    private val webSocketApi: WebSocketApi,
) : FlowUseCase<Unit, SocketTransfer>(coroutinesDispatcherProvider.io) {

    override suspend fun execute(params: Unit): Flow<Resource<SocketTransfer>> {
        return webSocketApi
            .receiveBinary("/$configFilePath", configFile.outputStream())
            .map { socketTransfer ->
                socketTransfer.toResource().onSuccess {
                    configFile.readText().let {
                        jsonPreferences.json = JSONObject(it)
                    }
                }
            }
    }

}
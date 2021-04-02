package com.viam.feeder.data.domain.config

import com.viam.feeder.core.network.CoroutinesDispatcherProvider
import com.viam.feeder.data.storage.JsonPreferences
import com.viam.feeder.di.AppModule.Companion.configFilePath
import com.viam.feeder.socket.WebSocketApi
import com.viam.feeder.socket.model.SocketTransfer
import dagger.hilt.android.scopes.ActivityScoped
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import org.json.JSONObject
import java.io.File
import javax.inject.Inject
import javax.inject.Named

@ActivityScoped
class GetConfig @Inject constructor(
    private val coroutinesDispatcherProvider: CoroutinesDispatcherProvider,
    @Named("configFile") private val configFile: File,
    private val jsonPreferences: JsonPreferences,
    private val webSocketApi: WebSocketApi,
) {
    operator fun invoke(): Flow<SocketTransfer> {
        val configParams =
            DownloadBinary.ConfigParams(configFilePath, configFile.outputStream())
        return webSocketApi.receiveBinary("/" + configParams.remotePath, configParams.outputStream)
            .flowOn(coroutinesDispatcherProvider.io)
            .map { socketTransfer ->
                if (socketTransfer is SocketTransfer.Success) {
                    configFile.readText().let {
                        jsonPreferences.json = JSONObject(it)
                    }
                }
                socketTransfer
            }
            .catch { e ->
                emit(SocketTransfer.Error(e))
            }
    }
}
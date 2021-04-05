package com.viam.feeder.data.domain.specification

import android.content.Context
import com.viam.feeder.core.network.CoroutinesDispatcherProvider
import com.viam.feeder.core.utility.convertToMp3
import com.viam.feeder.socket.WebSocketApi
import com.viam.feeder.socket.model.SocketTransfer
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.*
import javax.inject.Inject

open class ConvertUploadSound @Inject constructor(
    private val webSocketApi: WebSocketApi,
    private val coroutinesDispatcherProvider: CoroutinesDispatcherProvider,
    @ApplicationContext private val appContext: Context,
) {
    suspend operator fun invoke(parameters: ConvertUploadSoundParams): Flow<SocketTransfer> {
        val output = appContext.cacheDir.absolutePath + "/converted_" + parameters.filePath
        return flowOf(convertToMp3(parameters.filePath, output).inputStream())
            .flatMapLatest {
                webSocketApi.sendBinary(parameters.remotePath, it)
            }
            .flowOn(coroutinesDispatcherProvider.io)
            .catch { e ->
                emit(SocketTransfer.Error(e))
            }
    }

    data class ConvertUploadSoundParams(val remotePath: String, val filePath: String)

}
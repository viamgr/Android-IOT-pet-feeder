package com.viam.feeder.data.domain.specification

import android.content.Context
import com.viam.feeder.core.domain.FlowUseCase
import com.viam.feeder.core.network.CoroutinesDispatcherProvider
import com.viam.feeder.core.utility.convertToMp3
import com.viam.feeder.core.utility.toResource
import com.viam.resource.Resource
import com.viam.resource.onSuccess
import com.viam.websocket.WebSocketApi
import com.viam.websocket.model.SocketTransfer
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapConcat
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import java.io.File
import java.io.InputStream
import javax.inject.Inject

open class ConvertUploadSound @Inject constructor(
    private val webSocketApi: WebSocketApi,
    @ApplicationContext private val appContext: Context,
    coroutinesDispatcherProvider: CoroutinesDispatcherProvider
) : FlowUseCase<ConvertUploadSound.ConvertUploadSoundParams, SocketTransfer>(
    coroutinesDispatcherProvider.io
) {

    override fun execute(parameter: ConvertUploadSoundParams): Flow<Resource<SocketTransfer>> {
        val output =
            appContext.cacheDir.absolutePath + "/converted_" + parameter.remotePath.replace(
                "/",
                "_"
            )
        return flow<InputStream> {
            emit(convertToMp3(parameter.filePath, output).inputStream())
        }.map {
            webSocketApi.sendBinary(parameter.remotePath, it)
        }.flatMapConcat {
            it.map { socketTransfer ->
                socketTransfer.toResource().onSuccess {
                    File(output).deleteOnExit()
                }
            }
        }
    }

    data class ConvertUploadSoundParams(val remotePath: String, val filePath: String)

}
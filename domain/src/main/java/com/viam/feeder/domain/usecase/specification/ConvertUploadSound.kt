package com.viam.feeder.domain.usecase.specification

import android.content.Context
import com.viam.feeder.domain.base.CoroutinesDispatcherProvider
import com.viam.feeder.domain.base.FlowUseCase
import com.viam.feeder.domain.base.toResource
import com.viam.feeder.domain.repositories.socket.FfmpegRepository
import com.viam.feeder.domain.repositories.socket.WebSocketRepository
import com.viam.resource.Resource
import com.viam.resource.onSuccess
import com.viam.websocket.model.SocketTransfer
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapConcat
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import java.io.File
import java.io.InputStream
import javax.inject.Inject

open class ConvertUploadSound @Inject constructor(
    private val ffmpegRepository: FfmpegRepository,
    private val webSocketRepository: WebSocketRepository,
    @ApplicationContext private val appContext: Context,
    coroutinesDispatcherProvider: CoroutinesDispatcherProvider
) : FlowUseCase<ConvertUploadSound.ConvertUploadSoundParams, SocketTransfer>(
    coroutinesDispatcherProvider.io
) {

    @FlowPreview
    override fun execute(parameter: ConvertUploadSoundParams): Flow<Resource<SocketTransfer>> {
        val output =
            appContext.cacheDir.absolutePath + "/converted_" + parameter.remotePath.replace(
                "/",
                "_"
            )
        return flow<InputStream> {
            emit(ffmpegRepository.convertToMp3(parameter.filePath, output).inputStream())
        }.map {
            webSocketRepository.sendBinary(parameter.remotePath, it)
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
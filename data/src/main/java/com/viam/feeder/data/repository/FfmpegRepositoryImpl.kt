package com.viam.feeder.data.repository

import com.viam.feeder.data.datasource.ConvertDataSource
import com.viam.feeder.domain.repositories.socket.FfmpegRepository
import java.io.File
import javax.inject.Inject

class FfmpegRepositoryImpl @Inject constructor(private val convertDataSource: ConvertDataSource) :
    FfmpegRepository {
    override fun convertToMp3(input: String, output: String): File =
        convertDataSource.convertToMp3(input, output)

}
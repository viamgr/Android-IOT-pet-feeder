package com.viam.feeder.domain.repositories.socket

import java.io.File

interface FfmpegRepository {
    fun convertToMp3(input: String, output: String): File

}
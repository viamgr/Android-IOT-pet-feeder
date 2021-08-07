package com.viam.feeder.data.utils

import com.arthenica.mobileffmpeg.Config
import com.arthenica.mobileffmpeg.FFmpeg
import java.io.File

fun convertToMp3(input: String, output: String): File {
    val cmd =
        "-i $input -y -codec:a libmp3lame -ac 1 -ar 44100 -ab 128k -t 10  -map 0:a -map_metadata -1 $output"
    val rc: Int = FFmpeg.execute(cmd)
    if (rc == Config.RETURN_CODE_SUCCESS) {
        return File(output)
    } else {
        throw Exception(Config.getLastCommandOutput())
    }
}

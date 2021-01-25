package com.viam.feeder.core.utility

import android.content.Context
import com.viam.feeder.R

fun Long.convertSeconds(context: Context): String {
    val h = this / 3600
    val m = this % 3600 / 60
    val s = this % 60
    val sh = if (h > 0) context.getString(R.string.hour_format, h) else ""
    val sm =
        (if (m in 1..9 && h > 0) "0" else "") + if (m > 0) if (h > 0 && s == 0L) context.getString(
            R.string.minute_format,
            m
        ) else context.getString(
            R.string.minute_format,
            m
        ) else ""
    val ss =
        if (s == 0L && (h > 0 || m > 0)) "" else (if (s < 10 && (h > 0 || m > 0)) context.getString(
            R.string.second_format,
            s
        ) else "")
    return sh + (if (h > 0) " " else "") + sm + (if (m > 0) " " else "") + ss
}
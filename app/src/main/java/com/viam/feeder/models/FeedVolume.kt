package com.viam.feeder.models

data class FeedVolume(
    val duration: Int,
    val label: Int,
    var selected: Boolean = false
)
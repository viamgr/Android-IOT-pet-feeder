package com.viam.feeder.models

data class FeedVolume(
    val id: Int,
    val scale: Float,
    val label: Int,
    var selected: Boolean = false
)
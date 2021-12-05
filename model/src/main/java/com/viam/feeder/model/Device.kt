package com.viam.feeder.model

data class Device(
    var id: Long? = null,
    var name: String,
    val staticIp: String? = null,
    val port: Int? = 80,
    val gateway: String? = null,
    val subnet: String? = null,
    val useDhcp: Boolean? = null
)
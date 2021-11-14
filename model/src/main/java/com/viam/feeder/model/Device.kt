package com.viam.feeder.model

data class Device(
    val id: Long? = null,
    val name: String,
    val staticIp: String? = null,
    val port: Int? = 80,
    val gateway: String? = null,
    val subnet: String? = null,
    val useDhcp: Boolean? = null
)
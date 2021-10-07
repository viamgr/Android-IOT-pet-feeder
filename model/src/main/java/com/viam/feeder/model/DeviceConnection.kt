package com.viam.feeder.model

data class DeviceConnection(val host: String, val port: Int? = 80, val connectionType: ConnectionType)
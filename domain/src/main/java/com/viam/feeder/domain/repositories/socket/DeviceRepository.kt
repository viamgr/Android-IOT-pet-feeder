package com.viam.feeder.domain.repositories.socket

import com.viam.feeder.model.Device

interface DeviceRepository {
    fun getAll(): List<Device>
    fun insertAll(vararg devices: Device)
}
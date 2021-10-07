package com.viam.feeder.data.database.datasources

import com.viam.feeder.data.database.entities.Device

interface DeviceLocalDataSource {
    fun loadAllByIds(ids: IntArray): List<Device>
    fun getAll(): List<Device>
    fun insertAll(vararg devices: Device)
}
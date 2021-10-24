package com.viam.feeder.data.repository

import com.viam.feeder.data.database.datasources.DeviceLocalDataSource
import com.viam.feeder.domain.repositories.socket.DeviceRepository
import com.viam.feeder.model.Device
import javax.inject.Inject
import com.viam.feeder.data.database.entities.Device as DeviceEntity

class DeviceRepositoryImpl @Inject constructor(private val deviceLocalDataSource: DeviceLocalDataSource) :
    DeviceRepository {
    override fun getAll(): List<Device> = deviceLocalDataSource.getAll().map {
        Device(it.id, it.name, it.staticIp, it.port, it.gateway, it.subnet)
    }

    override fun insertAll(vararg devices: Device) {
        val arrayOfDevices = devices.map {
            DeviceEntity(it.id, it.name, it.staticIp, it.port, it.gateway, it.subnet)
        }.toTypedArray()
        deviceLocalDataSource.insertAll(*arrayOfDevices)
    }
}


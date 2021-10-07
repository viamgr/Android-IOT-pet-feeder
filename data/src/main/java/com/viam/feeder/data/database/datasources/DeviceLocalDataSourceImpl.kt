package com.viam.feeder.data.database.datasources

import com.viam.feeder.data.database.AppDatabase
import com.viam.feeder.data.database.entities.Device
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DeviceLocalDataSourceImpl @Inject constructor(val database: AppDatabase) :
    DeviceLocalDataSource {
    override fun loadAllByIds(ids: IntArray) = database.deviceDao().loadAllByIds(ids)
    override fun getAll() = database.deviceDao().getAll()
    override fun insertAll(vararg devices: Device) = database.deviceDao().insertAll(*devices)
}
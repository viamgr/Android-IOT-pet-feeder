package com.viam.feeder.data.database.daos

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.viam.feeder.data.database.entities.Device

@Dao
interface DeviceDao {
    @Query("SELECT * FROM device")
    fun getAll(): List<Device>

    @Query("SELECT * FROM device WHERE id IN (:deviceIds)")
    fun loadAllByIds(deviceIds: IntArray): List<Device>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(vararg devices: Device)

    @Delete
    fun delete(device: Device)
}
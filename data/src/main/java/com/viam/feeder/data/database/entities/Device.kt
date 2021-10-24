package com.viam.feeder.data.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Device(
    @PrimaryKey(autoGenerate = true) val id: Long? = null,
    val name: String,
    val staticIp: String? = null,
    val port: Int? = 80,
    val gateway: String? = null,
    val subnet: String? = null
)
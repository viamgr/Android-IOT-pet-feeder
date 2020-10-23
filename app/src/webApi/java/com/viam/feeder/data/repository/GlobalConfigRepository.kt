package com.viam.feeder.data.repository

import com.viam.feeder.data.models.MotorStatusRequest
import com.viam.feeder.data.remote.GlobalConfigService
import javax.inject.Inject

class GlobalConfigRepository @Inject constructor(private val globalConfigService: GlobalConfigService) {

    suspend fun getStatus() = globalConfigService.getStatus()
    suspend fun getMotorStatus() = globalConfigService.getMotorStatus()
    suspend fun setMotorStatus(motorStatusRequest: MotorStatusRequest) =
        globalConfigService.setMotorStatus(motorStatusRequest)

}
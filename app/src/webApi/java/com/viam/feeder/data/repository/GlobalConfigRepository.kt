package com.viam.feeder.data.repository

import com.viam.feeder.data.GlobalConfigService
import com.viam.feeder.data.models.MotorStatusRequest
import javax.inject.Inject

class GlobalConfigRepository @Inject constructor(private val globalConfigService: GlobalConfigService) {

    suspend fun getStatus() = globalConfigService.getStatus()
    suspend fun getMotorStatus() = globalConfigService.getMotorStatus()
    suspend fun setMotorStatus(motorStatusRequest: MotorStatusRequest) =
        globalConfigService.setMotorStatus(motorStatusRequest)

}
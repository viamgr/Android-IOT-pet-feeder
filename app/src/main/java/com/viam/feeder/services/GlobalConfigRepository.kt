package com.viam.feeder.services

import com.viam.feeder.services.models.MotorStatusRequest
import javax.inject.Inject

class GlobalConfigRepository @Inject constructor(private val globalConfigService: GlobalConfigService) {

    suspend fun getStatus() = globalConfigService.getStatus()
    suspend fun getMotorStatus() = globalConfigService.getMotorStatus()
    suspend fun setMotorStatus(motorStatusRequest: MotorStatusRequest) =
        globalConfigService.setMotorStatus(motorStatusRequest)

}
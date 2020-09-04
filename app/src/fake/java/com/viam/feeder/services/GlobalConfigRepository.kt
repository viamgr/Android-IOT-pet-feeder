package com.viam.feeder.services

import com.viam.feeder.services.models.MotorStatusRequest
import com.viam.feeder.services.models.MotorStatusResponse
import com.viam.feeder.services.models.Status
import javax.inject.Inject
import kotlin.random.Random

class GlobalConfigRepository @Inject constructor(private val globalConfigService: GlobalConfigService) {

    suspend fun getStatus(): Status {
        return if (Random.nextInt(0, 40) == 0) {
            Status(false)
        } else {
            Status(true)
        }
    }

    suspend fun getMotorStatus(): MotorStatusResponse {
        val nextInt = Random.nextInt(0, 10)
        return when {
            nextInt < 5 -> {
                MotorStatusResponse(enabled = false)
            }
            nextInt < 8 -> {
                MotorStatusResponse(enabled = true)
            }
            else -> {
                MotorStatusResponse(enabled = true)
            }
        }
    }

    suspend fun setMotorStatus(motorStatusRequest: MotorStatusRequest): MotorStatusResponse {
        val nextInt = Random.nextInt(0, 11)
        return when {
            nextInt < 5 -> {
                MotorStatusResponse(enabled = false)
            }
            nextInt < 8 -> {
                MotorStatusResponse(enabled = true)
            }
            nextInt < 9 -> {
                throw Exception("ERROR")
            }
            else -> {
                MotorStatusResponse(enabled = true)
            }
        }
    }

}
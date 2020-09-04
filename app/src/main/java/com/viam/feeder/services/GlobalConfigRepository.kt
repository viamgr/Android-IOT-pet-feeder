package com.viam.feeder.services

import javax.inject.Inject

class GlobalConfigRepository @Inject constructor(private val globalConfigService: GlobalConfigService) {

    suspend fun getStatus() = globalConfigService.getStatus()

}
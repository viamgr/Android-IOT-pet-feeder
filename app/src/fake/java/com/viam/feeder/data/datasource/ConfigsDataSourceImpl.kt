package com.viam.feeder.data.datasource

import com.viam.feeder.data.utils.fakeRequest
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ConfigsDataSourceImpl @Inject constructor() : ConfigsDataSource {

    override suspend fun downloadConfigs() = fakeRequest {
        "{\"feedingDuration\":60000,\"ledTurnOffDelay\":5000,\"ledState\":2,\"soundVolume\":3.99,\"wifiSsid\":\"V. M\",\"wifiPassword\":\"6037991302\",\"alarms\":[\"0 20 4 * * *\",\"0 30 4 * * *\",\"0 30 8 * * *\",\"0 30 12 * * *\",\"0 30 16 * * *\",\"0 30 7 * * *\"]}".byteInputStream()
    }

}
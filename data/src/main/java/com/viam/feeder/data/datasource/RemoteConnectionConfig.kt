package com.viam.feeder.data.datasource

import com.viam.feeder.shared.DEFAULT_ACCESS_POINT_IP
import com.viam.feeder.shared.DEFAULT_ACCESS_POINT_PORT
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RemoteConnectionConfig @Inject constructor() {
    var url: String = DEFAULT_ACCESS_POINT_IP
    var port: Int = DEFAULT_ACCESS_POINT_PORT
}

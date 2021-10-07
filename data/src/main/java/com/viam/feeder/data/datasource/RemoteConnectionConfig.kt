package com.viam.feeder.data.datasource

import com.viam.feeder.shared.DEFAULT_ACCESS_POINT_IP
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RemoteConnectionConfig @Inject constructor() {
    var url: String = DEFAULT_ACCESS_POINT_IP
}

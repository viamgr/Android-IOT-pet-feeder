package com.viam.feeder.data.datasource

import java.io.InputStream

interface ConfigsDataSource {
    suspend fun downloadConfigs(): InputStream
}
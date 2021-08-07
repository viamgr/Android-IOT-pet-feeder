package com.viam.feeder.data.repository

import com.viam.feeder.data.datasource.ConvertDataSource
import dagger.hilt.android.scopes.ActivityScoped
import javax.inject.Inject

@ActivityScoped
class ConvertRepository @Inject constructor(private val convertDataSource: ConvertDataSource) {
    suspend fun convertToMp3(input: String, output: String) =
        convertDataSource.convertToMp3(input, output)
}

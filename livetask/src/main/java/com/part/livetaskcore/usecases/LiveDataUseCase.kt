package com.part.livetaskcore.usecases

import androidx.lifecycle.LiveData
import androidx.lifecycle.liveData
import androidx.lifecycle.map
import com.viam.resource.Resource
import kotlinx.coroutines.CoroutineDispatcher

abstract class LiveDataUseCase<in P, R>(private val coroutineDispatcher: CoroutineDispatcher) {

    operator fun invoke(parameters: P): LiveData<Resource<R>> {
        return try {
            execute(parameters).map {
                Resource.Success(it)
            }
        } catch (e: Exception) {
            liveData {
                Resource.Error(e)
            }
        }
    }

    protected abstract fun execute(parameters: P): LiveData<R>
}
package com.viam.feeder.core.task

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import com.viam.feeder.core.Resource
import com.viam.feeder.core.livedata.Event
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GlobalRequest @Inject constructor() {

    private val _errorEvents = MediatorLiveData<Event<String?>>()
    val errorEvents: LiveData<Event<String?>> = _errorEvents

    private val _state = MutableLiveData<Resource<Unit>>()
    val state: LiveData<Resource<Unit>> = _state

    private var loadingCount = 0;
    fun newEvent(result: Resource<Any?>) {
        when (result) {
            is Resource.Error -> {
                _errorEvents.postValue(Event(result.exception.message))
                loadingCount--
            }
            is Resource.Success -> {
                loadingCount--
            }
            is Resource.Loading -> {
                loadingCount++
            }
        }

        if (loadingCount == 0) {
            if (result is Resource.Error) {
                _state.postValue(Resource.Error(result.exception))
            } else if (result is Resource.Success) {
                _state.postValue(Resource.Success(Unit))
            }
        } else {
            _state.postValue(Resource.Loading)
        }
    }

}
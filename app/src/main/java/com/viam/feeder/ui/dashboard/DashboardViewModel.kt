package com.viam.feeder.ui.dashboard

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.*
import com.viam.feeder.core.Resource
import com.viam.feeder.core.livedata.Event
import com.viam.feeder.core.network.CoroutinesDispatcherProvider
import com.viam.feeder.core.network.NetworkStatus
import com.viam.feeder.core.network.safeApiCall
import com.viam.feeder.services.GlobalConfigRepository
import com.viam.feeder.services.models.MotorStatusRequest
import com.viam.feeder.services.models.MotorStatusResponse
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class DashboardViewModel @ViewModelInject constructor(
    private val networkStatus: NetworkStatus,
    private val globalConfigRepository: GlobalConfigRepository,
    private val dispatcherProvider: CoroutinesDispatcherProvider
) :
    ViewModel() {

    private val _toggleMotorState = MutableLiveData<Event<Unit>>()
    val toggleMotorState: LiveData<Event<Unit>> = _toggleMotorState

    private val _motorStatus = MediatorLiveData<Resource<MotorStatusResponse>>()
    val motorStatus = _motorStatus.map {
        it is Resource.Success && it.data.enabled
    }

    init {
        _motorStatus.addSource(_toggleMotorState) {
            viewModelScope.launch {
                withContext(dispatcherProvider.io) {
                    _motorStatus.postValue(safeApiCall {
                        globalConfigRepository.setMotorStatus(MotorStatusRequest(enabled = true))
                    })
                }
            }
        }
        checkRealTimeStatus()
    }

    private fun checkRealTimeStatus() {
        viewModelScope.launch {
            withContext(dispatcherProvider.io) {
                networkStatus.runIfConnected {
                    _motorStatus.postValue(safeApiCall {
                        globalConfigRepository.getMotorStatus()
                    })
                }
                delay(10000)
                checkRealTimeStatus()
            }
        }
    }

    fun toggleMotorState() {
        _toggleMotorState.value = Event(Unit)
    }
}
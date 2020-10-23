package com.viam.feeder.ui.dashboard

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.*
import com.viam.feeder.MyApplication
import com.viam.feeder.core.Resource
import com.viam.feeder.core.livedata.Event
import com.viam.feeder.core.network.CoroutinesDispatcherProvider
import com.viam.feeder.core.network.NetworkStatus
import com.viam.feeder.core.network.safeApiCall
import com.viam.feeder.core.onError
import com.viam.feeder.core.onSuccess
import com.viam.feeder.data.models.MotorStatusRequest
import com.viam.feeder.data.models.MotorStatusResponse
import com.viam.feeder.data.repository.GlobalConfigRepository
import kotlinx.coroutines.launch

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

        }
        checkRealTimeStatus()
    }

    private fun checkRealTimeStatus() {
        /* viewModelScope.launch {
             withContext(dispatcherProvider.io) {
                 networkStatus.runIfConnected {
                     val safeApiCall = safeApiCall {
                         globalConfigRepository.getMotorStatus()
                     }
                     _motorStatus.postValue(safeApiCall)
                 }
                 delay(10000)
                 checkRealTimeStatus()
             }
         }*/
    }

    fun toggleMotorState() {
//        _toggleMotorState.value = Event(Unit)

        viewModelScope.launch(dispatcherProvider.io) {
            safeApiCall {
                globalConfigRepository.setMotorStatus(MotorStatusRequest(enabled = true))
            }
                .onSuccess {
                    MyApplication.toast(it.enabled.toString())
                }
                .onError {
                    MyApplication.toast(it.message!!)
                }
//            _motorStatus.postValue(safeApiCall)
        }

    }
}
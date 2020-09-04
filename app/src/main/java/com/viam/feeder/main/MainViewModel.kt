package com.viam.feeder.main

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.viam.feeder.core.network.CoroutinesDispatcherProvider
import com.viam.feeder.core.network.safeApiCall
import com.viam.feeder.core.onError
import com.viam.feeder.core.onSuccess
import com.viam.feeder.services.GlobalConfigRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.HttpException

class MainViewModel @ViewModelInject constructor(
    private val dispatcherProvider: CoroutinesDispatcherProvider,
    private val globalConfigRepository: GlobalConfigRepository
) :
    ViewModel() {

    companion object {
        const val CONNECTION_STATE_CONNECTING = 0
        const val CONNECTION_STATE_WRONG = 2
        const val CONNECTION_STATE_SUCCESS = 3
    }

    private val _connection = MutableLiveData<Int>(CONNECTION_STATE_CONNECTING)
    val connection: LiveData<Int> = _connection

    init {
        checkInternetConnection()
    }

    @Suppress("BlockingMethodInNonBlockingContext")
    fun checkInternetConnection() {
        viewModelScope.launch {
            withContext(dispatcherProvider.io) {
                safeApiCall {
                    globalConfigRepository.getStatus()
                }.onSuccess {
                    _connection.postValue(CONNECTION_STATE_SUCCESS)
                }.onError {
                    if (it !is HttpException || it.code() == 404) {
                        _connection.postValue(CONNECTION_STATE_WRONG)
                    }
                }
                delay(2000)
                checkInternetConnection()
            }

        }
    }

}

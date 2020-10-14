package com.viam.feeder.ui.setting

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.viam.feeder.core.dataOrNull
import com.viam.feeder.core.livedata.Event
import com.viam.feeder.data.models.WifiDevice
import com.viam.feeder.data.repository.WifiRepository
import kotlinx.coroutines.launch

class SettingViewModel @ViewModelInject constructor(private val wifiRepository: WifiRepository) :
    ViewModel() {

    private val _loading = MutableLiveData<Boolean>()
    val loading: LiveData<Boolean> = _loading

    private val _itemClicked = MutableLiveData<Event<WifiDevice>>()
    val itemClicked: LiveData<Event<WifiDevice>> = _itemClicked
    val controller = WifiController()
        .also { wifiController ->
            wifiController.clickListener = {
                _itemClicked.value = Event(it)
            }
        }

    fun onPasswordConfirmed(password: String) {
        _loading.postValue(true)
        viewModelScope.launch {
            _itemClicked.value?.peekContent()?.let {
                wifiRepository.connect(it.ssid, password)
                _loading.postValue(false)
            }
        }
    }

    private fun getWifiList() {
//        _loading.postValue(true)
        viewModelScope.launch {
            wifiRepository.getList().let {
                controller.setData(it.dataOrNull())
//                _loading.postValue(false)
            }
        }
    }

    init {
        getWifiList()
    }

}
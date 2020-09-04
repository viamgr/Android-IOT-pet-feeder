package com.viam.feeder.core.utility

import androidx.lifecycle.MutableLiveData

fun <T> MutableLiveData<T>.postValueIfChanged(inputValue: T) {
    if (value != inputValue) {
        postValue(inputValue)
    }
}
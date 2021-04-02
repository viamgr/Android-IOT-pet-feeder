package com.viam.feeder.core.utility

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

fun ViewModel.launchInScope(block: suspend () -> Unit) {
    viewModelScope.launch {
        block.invoke()
    }
}
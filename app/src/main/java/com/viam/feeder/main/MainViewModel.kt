package com.viam.feeder.main

import androidx.lifecycle.ViewModel
import com.markodevcic.peko.PermissionsLiveData

class MainViewModel : ViewModel() {
    val permissionLiveData = PermissionsLiveData()

    fun checkPermissions(vararg permissions: String) {
        permissionLiveData.checkPermissions(*permissions)
    }
}
package com.viam.feeder.core.utility

import android.Manifest
import com.viam.feeder.R

val appPermissionList = mapOf(
    Manifest.permission.RECORD_AUDIO to Pair(R.string.record_audio, R.string.record_audio_desc),
    Manifest.permission.WRITE_EXTERNAL_STORAGE to Pair(
        R.string.write_external_storage,
        R.string.write_external_storage_desc
    ),
    Manifest.permission.READ_EXTERNAL_STORAGE to Pair(
        R.string.read_external_storage,
        R.string.read_external_storage_desc
    ),
    Manifest.permission.ACCESS_FINE_LOCATION to Pair(
        R.string.access_fine_location,
        R.string.access_fine_location_desc
    ),
    Manifest.permission.ACCESS_COARSE_LOCATION to Pair(
        R.string.access_coarse_location,
        R.string.access_coarse_location_desc
    ),
    Manifest.permission.CHANGE_WIFI_STATE to Pair(
        R.string.change_wifi_state,
        R.string.change_wifi_state
    ),
    Manifest.permission.ACCESS_WIFI_STATE to Pair(
        R.string.access_wifi_state,
        R.string.access_wifi_state
    ),
    Manifest.permission.CHANGE_NETWORK_STATE to Pair(
        R.string.change_network_state,
        R.string.change_network_state
    ),
)




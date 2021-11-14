package com.viam.networkavailablity

data class NetworkStatus(val isAvailable: Boolean, val dnsServer: String? = null, val gateway: String?)
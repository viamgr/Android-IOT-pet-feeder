package com.viam.networkavailablity

import android.Manifest.permission
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.ConnectivityManager
import android.net.ConnectivityManager.NetworkCallback
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest.Builder
import android.os.Build.VERSION
import android.os.Build.VERSION_CODES
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import com.viam.networkavailablity.Connectivity.getDnsServer
import com.viam.networkavailablity.Connectivity.getGateway
import com.viam.networkavailablity.Connectivity.isWifiConnected
import com.viam.permissioncontract.PermissionContract
import com.viam.permissioncontract.permissionContract

class NetworkStatusObserver {
    private lateinit var permissionCallback: () -> Unit
    private lateinit var activity: AppCompatActivity
    private val permissionResult: PermissionContract<*> by lazy {
        activity.permissionContract()
    }

    private val connectivityManager: ConnectivityManager by lazy {
        activity.applicationContext.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    }
    private val networkCallback =
        if (VERSION.SDK_INT >= VERSION_CODES.LOLLIPOP) {
            object : NetworkCallback() {
                override fun onLost(network: Network) {
                    super.onLost(network)
                    setStatus(false)
                }

                override fun onUnavailable() {
                    setStatus(false)
                }

                override fun onLosing(network: Network, maxMsToLive: Int) {
                    setStatus(false)
                }

                override fun onAvailable(network: Network) {
                    setStatus(true)
                }
            }
        } else {
            null
        }

    private var startedNetworkListening = false
    private val wifiBroadcastReceiver: BroadcastReceiver by lazy {
        object : BroadcastReceiver() {

            @Suppress("DEPRECATION")
            private fun isConnectedOrConnecting(): Boolean {
                val networkInfo = connectivityManager.activeNetworkInfo
                return networkInfo != null && networkInfo.isConnectedOrConnecting
            }

            override fun onReceive(context: Context?, intent: Intent?) {
                setStatus(isConnectedOrConnecting())
            }
        }
    }

    fun withActivity(activity: AppCompatActivity): NetworkStatusObserver {
        this.activity = activity
        return this
    }

    fun start() {
        permissionResult.request(
            permission.CHANGE_NETWORK_STATE,
            permission.CHANGE_WIFI_STATE,
            permission.ACCESS_WIFI_STATE,
            permission.ACCESS_FINE_LOCATION,
            permission.ACCESS_COARSE_LOCATION,
            requiredPermissions = null
        ) {
            permissionCallback()
            startNetworkListening()
        }
    }

    fun stop() {
        startedNetworkListening = false
        try {
            if (VERSION.SDK_INT >= VERSION_CODES.LOLLIPOP) {
                connectivityManager.unregisterNetworkCallback(networkCallback!!)
            } else {
                activity.unregisterReceiver(wifiBroadcastReceiver)
            }
        } catch (e: Exception) {
        }
    }

    private val _networkStatus = MutableLiveData<NetworkStatus>()

    private fun setStatus(isAvailable: Boolean) {
        _networkStatus.postValue(
            NetworkStatus(
                isAvailable = isAvailable,
                dnsServer = activity.getDnsServer(),
                gateway = activity.getGateway()
            )
        )
    }

    private fun startNetworkListening() {
        startedNetworkListening = true

        setStatus(activity.isWifiConnected())

        if (VERSION.SDK_INT >= VERSION_CODES.LOLLIPOP) {
            val networkRequest = Builder()
                .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
                .addTransportType(NetworkCapabilities.TRANSPORT_CELLULAR)
                .build()

            connectivityManager.registerNetworkCallback(
                networkRequest,
                networkCallback!!
            )
        } else {
            @Suppress("DEPRECATION")
            activity.registerReceiver(
                wifiBroadcastReceiver,
                IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION)
            )
        }
    }

    fun observe(owner: LifecycleOwner, observer: Observer<NetworkStatus>): NetworkStatusObserver {
        _networkStatus.observe(owner, observer)
        return this
    }

    fun onPermissionCallback(block: () -> Unit): NetworkStatusObserver {
        this.permissionCallback = block
        return this
    }
}
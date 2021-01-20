package com.viam.feeder.ui.wifi

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.*
import com.viam.feeder.core.utility.dexter.PermissionContract
import com.viam.feeder.core.utility.dexter.permissionContract
import com.viam.feeder.ui.wifi.Connectivity.isConnected
import com.viam.feeder.ui.wifi.Connectivity.isWifi
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NetworkStatusObserver @Inject constructor() {
    private lateinit var activity: AppCompatActivity
    private val permissionResult: PermissionContract<*> by lazy {
        activity.permissionContract()
    }

    private val connectivityManager: ConnectivityManager by lazy {
        activity.applicationContext.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    }
    private val networkCallback = @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    object : ConnectivityManager.NetworkCallback() {
        override fun onLost(network: Network?) {
            setStatus(false)
        }

        override fun onUnavailable() {
            setStatus(false)
        }

        override fun onLosing(network: Network?, maxMsToLive: Int) {
            setStatus(false)
        }

        override fun onAvailable(network: Network?) {
            setStatus(true)
        }
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
            Manifest.permission.CHANGE_NETWORK_STATE,
            Manifest.permission.CHANGE_WIFI_STATE,
            Manifest.permission.ACCESS_WIFI_STATE,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            requiredPermissions = null
        ) {
            startNetworkListening()
        }
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    fun stopListen() {
        startedNetworkListening = false
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                connectivityManager.unregisterNetworkCallback(networkCallback)
            } else {
                activity.unregisterReceiver(wifiBroadcastReceiver)
            }
        } catch (e: Exception) {
        }
    }

    private fun getWifiName(): String? {
        return Connectivity.getWifiName(activity)
    }

    private val _networkStatus = MutableLiveData<NetworkStatus>()
    val networkStatus: LiveData<NetworkStatus> = _networkStatus

    private fun setStatus(isAvailable: Boolean) {
        _networkStatus.postValue(
            NetworkStatus(
                deviceName = getWifiName(),
                isAvailable = isAvailable,
                isWifi = isWifi(activity.applicationContext),
            )
        )
    }

    private fun startNetworkListening() {
        startedNetworkListening = true

        setStatus(isConnected(activity.applicationContext))

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            val networkRequest = NetworkRequest.Builder()
                .addTransportType(NetworkCapabilities.TRANSPORT_CELLULAR)
                .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
                .build()

            connectivityManager.registerNetworkCallback(
                networkRequest,
                networkCallback
            )

        } else {
            @Suppress("DEPRECATION")
            activity.registerReceiver(
                wifiBroadcastReceiver,
                IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION)
            )
        }
    }

    fun observe(owner: LifecycleOwner, observer: Observer<NetworkStatus>) {
        _networkStatus.observe(owner, observer)
    }
}
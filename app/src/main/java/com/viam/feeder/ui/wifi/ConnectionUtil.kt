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
import android.net.wifi.WifiManager
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.*
import com.viam.feeder.core.utility.dexter.permissionContract

class ConnectionUtil<T>(
    context: T,
) : LifecycleObserver where T : Context, T : LifecycleOwner {

    private val activity = context as AppCompatActivity
    private val connectivityManager: ConnectivityManager by lazy {
        activity.applicationContext.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    }
    private var listening = false
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
    private val permissionResult = activity.permissionContract()
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
    private val wifiManager: WifiManager by lazy {
        activity.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_CREATE)
    fun onCreate() {
        permissionResult.request(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            requiredPermissions = emptyArray()
        ) {
            startListen()
        }
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    fun onStart() {
        if (!listening && !permissionResult.isRequesting) {
            startListen()
        }
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    fun stopListen() {
        listening = false
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                connectivityManager.unregisterNetworkCallback(networkCallback)
            } else {
                activity.unregisterReceiver(wifiBroadcastReceiver)
            }
        } catch (e: Exception) {
        }
    }

    /**
     * Fetches Name of Current Wi-fi Access Point
     *
     * Returns blank string if received "SSID <unknown ssid>" which you get when location is turned off
     */
    private fun WifiManager.deviceName(): String? = connectionInfo.ssid.run {
        if (this.contains("<unknown ssid>")) null else this
    }

    private fun getWifiName(): String? {
        return wifiManager.deviceName()
    }

    private fun setStatus(status: Boolean) {
        val connectedWifi = Connectivity.isConnectedWifi(activity.applicationContext)
        val deviceName = if (connectedWifi) {
            getWifiName()
        } else {
            null
        }
        liveData.postValue(
            NetworkStatus(
                deviceName = deviceName,
                isAvailable = status,
                isWifi = connectedWifi
            )
        )
    }

    private fun startListen() {
        listening = true
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            val networkRequest = NetworkRequest.Builder()
                .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
                .addTransportType(NetworkCapabilities.TRANSPORT_CELLULAR)
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

    companion object {
        private val liveData = MutableLiveData<NetworkStatus>()
        val connectionState: LiveData<NetworkStatus> = liveData
    }

    init {
        context.lifecycle.addObserver(this)
    }
}

fun AppCompatActivity.startConnectionListener() {
    ConnectionUtil(this)
}
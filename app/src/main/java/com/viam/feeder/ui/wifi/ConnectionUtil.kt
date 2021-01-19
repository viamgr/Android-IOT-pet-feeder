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
import com.viam.feeder.core.utility.dexter.PermissionContract
import com.viam.feeder.core.utility.dexter.permissionContract

internal typealias ConnectionListener = (networkStatus: NetworkStatus) -> Unit

class ConnectionUtil<T>(
    context: AppCompatActivity,
    private val listener: ConnectionListener
) : LifecycleObserver where T : Context, T : LifecycleOwner {


    private val activity = context as AppCompatActivity
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
    private val permissionResult: PermissionContract<*> = activity.permissionContract()
    private var startNetworkListening = false
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
            Manifest.permission.CHANGE_NETWORK_STATE,
            Manifest.permission.CHANGE_WIFI_STATE,
            Manifest.permission.ACCESS_WIFI_STATE,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            requiredPermissions = null
        ) {
            startListen()
        }
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    fun onStart() {
        if (!startNetworkListening) {
            /*if (preferredWifiNetWorkSsid.isNullOrEmpty() || permissionResult.isGranted()) {
                startListen()
            }*/
        }
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    fun stopListen() {
        startNetworkListening = false
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

    private fun setStatus(isAvailable: Boolean) {
        listener(
            NetworkStatus(
                deviceName = getWifiName(),
                isAvailable = isAvailable,
                isWifi = isWifi(activity.applicationContext),
            )
        )
    }

    fun isWifi(context: Context?): Boolean {
        if (context == null) return false
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val capabilities =
                connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork)
            if (capabilities != null) {
                when {
                    capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> {
                        return true
                    }
                }
            }
        } else {
            val wifiManager =
                context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
            return wifiManager.isWifiEnabled
        }
        return false
    }

    private fun startListen() {
        startNetworkListening = true
        startNetworkListening()
    }

    private fun startNetworkListening() {
        setStatus(Connectivity.isConnected(activity.applicationContext))

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

    companion object {
        private val aConnectionState = MutableLiveData<NetworkStatus>()
        val connectionState: LiveData<NetworkStatus> = aConnectionState
    }

    init {
        context.lifecycle.addObserver(this)
    }
}

fun AppCompatActivity.startConnectionListener(listener: ConnectionListener): ConnectionUtil<AppCompatActivity> {
    return ConnectionUtil(this, listener)
}
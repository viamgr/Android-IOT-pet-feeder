package com.viam.feeder.ui.wifi

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.*
import android.net.wifi.WifiConfiguration
import android.net.wifi.WifiManager
import android.net.wifi.WifiNetworkSpecifier
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.*
import com.viam.feeder.core.utility.dexter.permissionContract


class ConnectionUtil<T>(
    context: T,
    private val preferredWifiNetWorkSsid: String?,
    private val preferredWifiNetWorkPassword: String?,
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
        if (!preferredWifiNetWorkSsid.isNullOrEmpty()) {
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
        } else {
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
        aConnectionState.postValue(
            NetworkStatus(
                deviceName = deviceName,
                isAvailable = status,
                isWifi = connectedWifi
            )
        )
    }

    @Suppress("DEPRECATION")
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
            activity.registerReceiver(
                wifiBroadcastReceiver,
                IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION)
            )
        }

        preferredWifiNetWorkSsid?.let {

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                connectToPreferredWifiNewVersions()
            } else {
                connectToPreferredWifiOldVersions()
            }

        }
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun connectToPreferredWifiNewVersions() {
        val specifier = WifiNetworkSpecifier.Builder()
            .setSsid(preferredWifiNetWorkSsid!!)
            .setWpa2Passphrase(preferredWifiNetWorkPassword!!)
            .build()

        val request = NetworkRequest.Builder()
            .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
            .removeCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .setNetworkSpecifier(specifier)
            .build()

        val connectivityManager =
            activity.applicationContext.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        val networkCallback = object : ConnectivityManager.NetworkCallback() {

            override fun onAvailable(network: Network?) {
                connectivityManager.requestNetwork(request, networkCallback)
            }

            override fun onLosing(network: Network, maxMsToLive: Int) {
                super.onLosing(network, maxMsToLive)
            }

            override fun onLost(network: Network) {
                super.onLost(network)
            }

            override fun onCapabilitiesChanged(
                network: Network,
                networkCapabilities: NetworkCapabilities
            ) {
                super.onCapabilitiesChanged(network, networkCapabilities)
            }

            override fun onLinkPropertiesChanged(network: Network, linkProperties: LinkProperties) {
                super.onLinkPropertiesChanged(network, linkProperties)
            }

            override fun onBlockedStatusChanged(network: Network, blocked: Boolean) {
                super.onBlockedStatusChanged(network, blocked)
            }

            override fun onUnavailable() {
                connectivityManager.requestNetwork(request, networkCallback)
            }
        }
        connectivityManager.requestNetwork(request, networkCallback)


    }

    @Suppress("DEPRECATION")
    private fun connectToPreferredWifiOldVersions() {
        val conf = WifiConfiguration()
        conf.SSID = "\"" + preferredWifiNetWorkSsid + "\""
        conf.wepKeys[0] = "\"" + preferredWifiNetWorkPassword + "\""
        conf.wepTxKeyIndex = 0
        conf.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE)
        conf.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP40)
        conf.preSharedKey = "\"" + preferredWifiNetWorkPassword + "\""
        val wifiManager =
            activity.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
        val networkId = wifiManager.addNetwork(conf)
        val wifiInfo = wifiManager.connectionInfo
        wifiManager.disableNetwork(wifiInfo.networkId)
        wifiManager.enableNetwork(networkId, true)
    }

    companion object {
        private val aConnectionState = MutableLiveData<NetworkStatus>()
        val connectionState: LiveData<NetworkStatus> = aConnectionState
    }

    init {
        context.lifecycle.addObserver(this)
    }
}

fun AppCompatActivity.startConnectionListener(
    preferredWifiNetWorkSsid: String? = null,
    preferredWifiNetWorkPassword: String? = null
) {
    val connection = ConnectionUtil(this, preferredWifiNetWorkSsid, preferredWifiNetWorkPassword)
}
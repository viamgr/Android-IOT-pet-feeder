package com.viam.feeder.ui.wifi

import android.app.Activity
import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.net.wifi.WifiManager
import android.net.wifi.WifiNetworkSpecifier
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.LifecycleOwner

class WifiAutoConnect<T>(
    private val fragmentActivity: T,
    private val preferredWifiNetWorkSsid: String?,
    private val preferredWifiNetWorkPassword: String?,
    private val availableCallback: (Boolean) -> Unit
) : LifecycleObserver where T : LifecycleOwner {

    val connectivityManager by lazy {
        requireContext().applicationContext.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    }
    private val networkCallback by lazy {
        @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
        object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network?) {
                connectivityManager.unregisterNetworkCallback(this)
                availableCallback(true)
            }

            override fun onUnavailable() {
                availableCallback(false)
                stop()
                super.onUnavailable()
            }
        }
    }

    fun start() {
        startListen()
    }

    fun stop() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            try {
                connectivityManager.unregisterNetworkCallback(networkCallback)
            } catch (e: Exception) {
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


        connectivityManager.requestNetwork(request, networkCallback)
    }

    @Suppress("DEPRECATION")
    private fun connectToPreferredWifiOldVersions() {
        val conf = android.net.wifi.WifiConfiguration()
        conf.SSID = "\"" + preferredWifiNetWorkSsid + "\""
        conf.wepKeys[0] = "\"" + preferredWifiNetWorkPassword + "\""
        conf.wepTxKeyIndex = 0
        conf.allowedKeyManagement.set(android.net.wifi.WifiConfiguration.KeyMgmt.NONE)
        conf.allowedGroupCiphers.set(android.net.wifi.WifiConfiguration.GroupCipher.WEP40)
        conf.preSharedKey = "\"" + preferredWifiNetWorkPassword + "\""
        val wifiManager =
            requireContext().applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
        val networkId = wifiManager.addNetwork(conf)
        val wifiInfo = wifiManager.connectionInfo
        wifiManager.disableNetwork(wifiInfo.networkId)
        wifiManager.enableNetwork(networkId, true)
    }

    private fun isFragment() = fragmentActivity is Fragment
    private fun requireContext(): Context {
        return if (isFragment()) (fragmentActivity as Fragment).requireActivity() else (fragmentActivity as Activity)
    }

    private fun startListen() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            connectToPreferredWifiNewVersions()
        } else {
            connectToPreferredWifiOldVersions()
        }
    }

    init {
        if (isFragment()) {
            (fragmentActivity as LifecycleOwner).lifecycle.addObserver(this)

        } else {
            (fragmentActivity as AppCompatActivity).lifecycle.addObserver(this)
        }
    }
}

fun Fragment.wifiAutoConnect(
    preferredWifiNetWorkSsid: String? = null,
    preferredWifiNetWorkPassword: String? = null, availableCallback: (Boolean) -> Unit
): WifiAutoConnect<Fragment> {
    return WifiAutoConnect(
        this,
        preferredWifiNetWorkSsid,
        preferredWifiNetWorkPassword,
        availableCallback
    )
}
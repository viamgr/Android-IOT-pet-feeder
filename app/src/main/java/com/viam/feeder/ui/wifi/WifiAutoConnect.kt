package com.viam.feeder.ui.wifi

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.net.wifi.WifiManager
import android.net.wifi.WifiNetworkSpecifier
import android.os.Build
import androidx.annotation.RequiresApi
import javax.inject.Inject


class WifiAutoConnect @Inject constructor() {
    private lateinit var context: Context

    private var preferredWifiNetWorkSsid: String? = null
    private var preferredWifiNetWorkPassword: String? = null
    private var resultListener: ((Boolean) -> Unit)? = null

    val connectivityManager by lazy {
        context.applicationContext.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    }
    private val networkCallback by lazy {
        @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
        object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
//                connectivityManager.unregisterNetworkCallback(this)
                resultListener?.invoke(true)
            }

            override fun onUnavailable() {
                resultListener?.invoke(false)
                super.onUnavailable()
            }
        }
    }

    fun startConnecting(
        ssid: String,
        password: String?,
        resultListener: ((Boolean) -> Unit)? = null
    ) {
        this.preferredWifiNetWorkSsid = ssid
        this.preferredWifiNetWorkPassword = password
        this.resultListener = resultListener
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
            .setNetworkSpecifier(specifier)
            .build()

        connectivityManager.requestNetwork(
            request,
            networkCallback
        )
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
            context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
        val networkId = wifiManager.addNetwork(conf)
        val wifiInfo = wifiManager.connectionInfo
        wifiManager.disableNetwork(wifiInfo.networkId)
        wifiManager.enableNetwork(networkId, true)
    }

    private fun startListen() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            connectToPreferredWifiNewVersions()
        } else {
            connectToPreferredWifiOldVersions()
        }
    }

    fun withContext(context: Context): WifiAutoConnect {
        this.context = context
        return this
    }
}
package com.part.livetaskcore.connection

import android.annotation.TargetApi
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Context.CONNECTIVITY_SERVICE
import android.content.Intent
import android.content.IntentFilter
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkRequest
import android.os.Build
import androidx.annotation.RequiresApi

/**
 * An observable class that its value indicates the connectivity status.
 * */
class ConnectionManager(private val context: Context) {

    private var onStatusChangeListener: ((isConnected: Boolean) -> Unit)? = null
    private var connectivityManager: ConnectivityManager =
        context.getSystemService(CONNECTIVITY_SERVICE) as ConnectivityManager

    private lateinit var connectivityManagerCallback: ConnectivityManager.NetworkCallback

    init {
        start()
    }

    fun setOnStatusChangeListener(onStatusChangeListener: (isConnected: Boolean) -> Unit) {
        this.onStatusChangeListener = onStatusChangeListener
    }

    fun start() {
//        updateConnection(activeNetwork?.isConnected == true)
        when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.N -> connectivityManager.registerDefaultNetworkCallback(
                registerConnectivityManager()
            )
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP -> lollipopNetworkAvailableRequest()
            else -> {
                context.registerReceiver(
                    networkReceiver,
                    IntentFilter("android.net.conn.CONNECTIVITY_CHANGE")
                )
            }
        }
    }

    fun stop() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            connectivityManager.unregisterNetworkCallback(connectivityManagerCallback)
        } else {
            context.unregisterReceiver(networkReceiver)
        }
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private fun lollipopNetworkAvailableRequest() {
        val builder = NetworkRequest.Builder()
            .addTransportType(android.net.NetworkCapabilities.TRANSPORT_VPN)
            .addTransportType(android.net.NetworkCapabilities.TRANSPORT_CELLULAR)
            .addTransportType(android.net.NetworkCapabilities.TRANSPORT_WIFI)
        connectivityManager.registerNetworkCallback(
            builder.build(),
            registerConnectivityManager()
        )
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    private fun registerConnectivityManager(): ConnectivityManager.NetworkCallback {
        connectivityManagerCallback =
            object : ConnectivityManager.NetworkCallback() {
                override fun onAvailable(network: Network) {
                    updateConnection(true)
                    super.onAvailable(network)
                }

                override fun onLost(network: Network) {
                    updateConnection(false)
                    super.onLost(network)
                }
            }
        return connectivityManagerCallback
    }

    private val networkReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val activeNetwork = connectivityManager.activeNetworkInfo

            updateConnection(activeNetwork?.isConnected == true)
        }
    }

    private fun updateConnection(isConnected: Boolean) {
        onStatusChangeListener?.invoke(isConnected)
    }


}
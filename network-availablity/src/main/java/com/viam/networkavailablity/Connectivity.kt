package com.viam.networkavailablity

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.wifi.WifiManager
import android.os.Build
import java.net.InetAddress
import java.net.NetworkInterface
import java.nio.ByteBuffer
import java.nio.ByteOrder

object Connectivity {
    fun getIpv4HostAddress(): String? =
        NetworkInterface.getNetworkInterfaces()?.toList()?.mapNotNull { networkInterface ->
            networkInterface.inetAddresses?.toList()
                ?.filter { !it.isLoopbackAddress && it.hostAddress.indexOf(':') < 0 }
                ?.mapNotNull { if (it.hostAddress.isNullOrBlank()) null else it.hostAddress }
                ?.firstOrNull { it.isNotEmpty() }
        }?.firstOrNull()

    fun isConnected(context: Context?): Boolean {
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
                    capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> {
                        return true
                    }
                    capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> {
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

    fun Context.isWifiConnected(): Boolean {
        val connectivityManager =
            applicationContext.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val capabilities =
                connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork)
            if (capabilities != null && capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {
                return true
            }
        } else {
            val wifiManager =
                applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
            return wifiManager.isWifiEnabled
        }
        return false
    }

    fun Context.getDnsServer(): String? {
        val wifiManager = getWifiManager()

        return InetAddress.getByAddress(
            ByteBuffer
                .allocate(4)
                .order(ByteOrder.LITTLE_ENDIAN)
                .putInt(wifiManager.dhcpInfo.dns1)
                .array()
        ).hostAddress
    }

    fun Context.getGateway(): String? {
        val wifiManager = getWifiManager()

        return InetAddress.getByAddress(
            ByteBuffer
                .allocate(4)
                .order(ByteOrder.LITTLE_ENDIAN)
                .putInt(wifiManager.dhcpInfo.gateway)
                .array()
        ).hostAddress
    }

    private fun Context.getWifiManager() =
        applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager

    /**
     * Fetches Name of Current Wi-fi Access Point
     *
     * Returns blank string if received "SSID <unknown ssid>" which you get when location is turned off
     */
    fun Context.getWifiName(): String? {
        val wifiManager =
            applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager

        return wifiManager.connectionInfo.ssid.run {
            if (this.contains("<unknown ssid>")) null else this.replace("\"", "")
        }
    }

    fun Context.isUnknownOrKnownWifiConnection(ssid: String): Boolean {
        val deviceName = getWifiName()
        return isWifiConnected() && (deviceName == null || deviceName == ssid)
    }
}
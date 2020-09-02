@file:Suppress("DEPRECATION")

package com.viam.feeder.wifi

import android.Manifest
import android.content.Context
import android.content.Intent
import android.net.wifi.SupplicantState
import android.net.wifi.WifiConfiguration
import android.net.wifi.WifiManager
import android.net.wifi.WifiNetworkSuggestion
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.observe
import com.markodevcic.peko.PermissionResult
import com.viam.feeder.R
import com.viam.feeder.databinding.FragmentWifiBinding
import com.viam.feeder.databinding.viewBinding
import com.viam.feeder.livedata.EventObserver
import com.viam.feeder.main.MainViewModel
import com.viam.feeder.wifi.WifiViewModel.Companion.CURRENT_STATUS_CONNECTING
import com.viam.feeder.wifi.WifiViewModel.Companion.CURRENT_STATUS_DISABLED
import com.viam.feeder.wifi.WifiViewModel.Companion.CURRENT_STATUS_DONE
import com.viam.feeder.wifi.WifiViewModel.Companion.CURRENT_STATUS_MANUALLY
import com.viam.feeder.wifi.WifiViewModel.Companion.CURRENT_STATUS_RETRY
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ObsoleteCoroutinesApi
import kotlinx.coroutines.channels.ticker
import kotlinx.coroutines.launch


/**
 * A simple [Fragment] subclass as the default destination in the navigation.
 */
@AndroidEntryPoint
@ObsoleteCoroutinesApi
class WifiFragment : Fragment(R.layout.fragment_wifi) {

    private var lastTime: Long = 0
    private val binding by viewBinding(FragmentWifiBinding::bind)

    private val wifiManager: WifiManager
        get() = requireActivity().applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager

    private val viewModel: WifiViewModel by viewModels()


    private fun setLastState(lastState: Int) {
        viewModel.setWifiState(lastState)
    }
    private var lastAskedPermission: PermissionResult? = null

    private val activityViewModels = activityViewModels<MainViewModel>()
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.apply {
            lifecycleOwner = viewLifecycleOwner
            vm = viewModel
        }

        viewModel.retry.observe(viewLifecycleOwner, EventObserver {
            setWifiListener()
        })
        viewModel.enableWifiClicked.observe(viewLifecycleOwner, EventObserver {
            val intent = Intent(Settings.ACTION_WIFI_SETTINGS)
            if (intent.resolveActivity(requireActivity().packageManager) != null) {
                startActivity(intent)
            }
        })


        activityViewModels.value.permissionLiveData.observe(
            requireActivity(),
            ::onPermissionLiveData
        )

    }

    private val ssid = "V. M"
    private val password = "6037991302"


    @Suppress("DEPRECATION")
    private fun tryConnectWifi() {
        Toast.makeText(requireContext(), "tryConnectWifi", Toast.LENGTH_SHORT).show()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val networkSuggestion1 =
                WifiNetworkSuggestion.Builder()
                    .setSsid(ssid)
                    .setWpa2Passphrase(password)
                    .build()
            wifiManager.addNetworkSuggestions(arrayListOf(networkSuggestion1))
        } else {
            val wifiConfiguration = WifiConfiguration()
            wifiConfiguration.SSID = String.format("\"%s\"", ssid)
            wifiConfiguration.preSharedKey = String.format("\"%s\"", password)
            val wifiID = wifiManager.addNetwork(wifiConfiguration)
            wifiManager.enableNetwork(wifiID, true)
        }
        setLastState(CURRENT_STATUS_CONNECTING)

    }

    @ObsoleteCoroutinesApi
    private fun setWifiListener() {
        val tickerChannel = ticker(delayMillis = 1_000, initialDelayMillis = 0)
        lastTime = System.currentTimeMillis()
        lifecycleScope.launch {
            for (event in tickerChannel) {
                if (viewModel.wifiState.value == CURRENT_STATUS_CONNECTING && System.currentTimeMillis() - lastTime > 10_000) {
                    retryConnecting()
                    tickerChannel.cancel()
                } else if (wifiManager.isWifiEnabled) {
                    if (isDeviceConnected()) {
                        lastTime = System.currentTimeMillis()
                        setLastState(CURRENT_STATUS_DONE)
                    } else {
                        checkAskedPermission()
                    }
                } else {
                    setLastState(CURRENT_STATUS_DISABLED)
                }

            }
        }
    }

    private fun checkAskedPermission() {
        if (lastAskedPermission == null || lastAskedPermission is PermissionResult.Granted) {
            tryRequestPermissions()
        } else {
            manuallyConnecting()
        }
    }

    private fun manuallyConnecting() {
        setLastState(CURRENT_STATUS_MANUALLY)
    }

    private fun isDeviceConnected(): Boolean {
        val info = wifiManager.connectionInfo
//        Toast.makeText(requireContext(), "${info.ssid}", Toast.LENGTH_SHORT).show()
        return info.ssid == "\"$ssid\"" && wifiManager.connectionInfo.supplicantState == SupplicantState.COMPLETED
    }

    private fun tryRequestPermissions() {
        activityViewModels.value.checkPermissions(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )

    }

    private fun onPermissionLiveData(result: PermissionResult) {
        lastAskedPermission = result
        if (result is PermissionResult.Granted) {
            tryConnectWifi()
            setLastState(CURRENT_STATUS_CONNECTING)
        } else {
            manuallyConnecting()
        }
    }

    private fun retryConnecting() {
        setLastState(CURRENT_STATUS_RETRY)
    }

}
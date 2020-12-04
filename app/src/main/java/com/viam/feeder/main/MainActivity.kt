package com.viam.feeder.main

import android.os.Bundle
import android.os.Handler
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.postDelayed
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.NavigationUI
import androidx.navigation.ui.setupActionBarWithNavController
import com.google.android.material.snackbar.Snackbar
import com.viam.feeder.R
import com.viam.feeder.constants.ACCESS_POINT_SSID
import com.viam.feeder.core.domain.utils.isConnectionError
import com.viam.feeder.core.domain.utils.toMessage
import com.viam.feeder.core.livedata.EventObserver
import com.viam.feeder.core.onError
import com.viam.feeder.core.task.AutoRetryHandler
import com.viam.feeder.core.task.TaskEventLogger
import com.viam.feeder.ui.wifi.NetworkStatus
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.content_main.bottom_nav

@AndroidEntryPoint

class MainActivity : AppCompatActivity() {

    private var backPressedOnce = false
    private lateinit var navController: NavController
    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)
        setupViews()


        TaskEventLogger.events.observe(this, EventObserver { resource ->
            resource?.onError {
                if (it.isConnectionError() && !viewModel.isWifiDialogShowing) {
                    setIsWifiDialogShowing(true)
                    navController.navigate(R.id.wifi_fragment)
                } else {
                    showMessage(it.toMessage(this))
                }
            }
        })

        viewModel.connectionStatus.observe(this, {
            if (!isConnectedToPreferredDevice(it)) {
                setIsWifiDialogShowing(true)
                navController.navigate(R.id.wifi_fragment)
            }
        })

        viewModel.connectionStatus.observe(this, {
            AutoRetryHandler.value = it.isAvailable
        })

    }

    private fun showMessage(message: String) {
        Snackbar.make(
            findViewById(android.R.id.content),
            message,
            Snackbar.LENGTH_SHORT
        ).show()
    }

    private fun isConnectedToPreferredDevice(networkState: NetworkStatus): Boolean {
        return networkState.isAvailable && networkState.isWifi &&
                (networkState.deviceName == null ||
                        networkState.deviceName == "\"$ACCESS_POINT_SSID\"")
    }

    override fun onBackPressed() {
        if (navController.graph.startDestination == navController.currentDestination?.id) {
            if (backPressedOnce) {
                super.onBackPressed()
                return
            }

            backPressedOnce = true
            Toast.makeText(this, "Press BACK again to exit", Toast.LENGTH_SHORT).show()

            Handler().postDelayed(2000) {
                backPressedOnce = false
            }
        } else {
            super.onBackPressed()
        }
    }

    fun setIsWifiDialogShowing(isShowing: Boolean) {
        viewModel.isWifiDialogShowing = isShowing
    }

    private fun setupViews() {
        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_container) as NavHostFragment
        navController = navHostFragment.navController
        NavigationUI.setupWithNavController(bottom_nav, navHostFragment.navController)

        //var appBarConfiguration = AppBarConfiguration(navHostFragment.navController.graph)
        val appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.fragment_home,
                R.id.fragment_specification,
                R.id.fragment_timer,
                R.id.fragment_setting
            )
        )
        setupActionBarWithNavController(navHostFragment.navController, appBarConfiguration)
    }
}
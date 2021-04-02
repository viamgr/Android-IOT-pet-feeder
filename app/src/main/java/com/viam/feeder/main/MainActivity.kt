package com.viam.feeder.main

import android.os.Bundle
import android.os.Handler
import android.widget.Toast
import android.widget.Toast.LENGTH_SHORT
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.postDelayed
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.NavigationUI
import androidx.navigation.ui.setupActionBarWithNavController
import com.viam.feeder.R
import com.viam.feeder.constants.ACCESS_POINT_SSID
import com.viam.feeder.core.domain.utils.isConnectionError
import com.viam.feeder.core.domain.utils.toMessage
import com.viam.feeder.core.livedata.EventObserver
import com.viam.feeder.core.onError
import com.viam.feeder.core.task.AutoRetryHandler
import com.viam.feeder.core.task.TaskEventLogger
import com.viam.feeder.core.utility.bindingAdapter.contentView
import com.viam.feeder.databinding.ActivityMainBinding
import com.viam.feeder.ui.wifi.Connectivity.isUnknownOrKnownWifiConnection
import com.viam.feeder.ui.wifi.NetworkStatusObserver
import com.viam.feeder.ui.wifi.WifiFragmentDirections
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber
import javax.inject.Inject

@AndroidEntryPoint

class MainActivity : AppCompatActivity() {

    private var backPressedOnce = false
    private lateinit var navController: NavController
    private val viewModel: MainViewModel by viewModels()
    private val binding by contentView<MainActivity, ActivityMainBinding>(R.layout.activity_main)

    @Inject
    lateinit var networkStatusObserver: NetworkStatusObserver

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding.apply {
            lifecycleOwner = this@MainActivity
            setSupportActionBar(toolbar)
        }

        setupViews()
        networkStatusObserver
            .withActivity(this)
            .onPermissionCallback {
                viewModel.askedWifiPermissions.set(true)
            }
            .observe(this) {
                AutoRetryHandler.value = it.isAvailable
                val isUnknownOrKnownWifiConnection =
                    isUnknownOrKnownWifiConnection(ACCESS_POINT_SSID)
                if (!viewModel.isWifiDialogShowing && !isUnknownOrKnownWifiConnection) {
                    setIsWifiDialogShowing(true)
                    navController.navigate(WifiFragmentDirections.toWifiFragment(true))
                }
            }
            .start()

        TaskEventLogger.events.observe(this, EventObserver { resource ->
            resource?.onError {
                Timber.e(it)
                if (it.isConnectionError() && !viewModel.isWifiDialogShowing && viewModel.askedWifiPermissions.get()) {
                    setIsWifiDialogShowing(true)
                    navController.navigate(WifiFragmentDirections.toWifiFragment(false))
                } else {
                    showMessage(it.toMessage(this))
                }
            }
        })
        viewModel.downloadConfigProgress.observe(this) {
            print("downloadConfig")
            println(it)
        }
        viewModel.uploadFileProgress.observe(this) {
            print("uploadConfig")
            println(it)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        networkStatusObserver.stop()
    }

    private fun showMessage(message: String) {
        Toast.makeText(this, message, LENGTH_SHORT).show()
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
        navController = findNavController(R.id.nav_host_container)
        NavigationUI.setupWithNavController(binding.bottomNav, navController)

        //var appBarConfiguration = AppBarConfiguration(navHostFragment.navController.graph)
        val appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.fragment_home,
                R.id.fragment_timer,
                R.id.fragment_setting
            )
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
    }
}
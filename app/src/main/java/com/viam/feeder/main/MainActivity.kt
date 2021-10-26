package com.viam.feeder.main

import android.os.Bundle
import android.os.Handler
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.postDelayed
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.NavigationUI
import androidx.navigation.ui.setupActionBarWithNavController
import com.viam.feeder.R
import com.viam.feeder.core.utility.bindingAdapter.contentView
import com.viam.feeder.core.utility.reactToTask
import com.viam.feeder.databinding.ActivityMainBinding
import com.viam.feeder.domain.usecase.ConnectionStatus.NetworkOptions
import com.viam.feeder.ui.wifi.WifiAutoConnect
import com.viam.networkavailablity.Connectivity.getWifiName
import com.viam.networkavailablity.Connectivity.isWifiConnected
import com.viam.networkavailablity.NetworkStatusObserver
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private var backPressedOnce = false
    private lateinit var navController: NavController
    private val viewModel: MainViewModel by viewModels()
    private val binding by contentView<MainActivity, ActivityMainBinding>(R.layout.activity_main)

    @Inject
    lateinit var networkStatusObserver: NetworkStatusObserver

    @Inject
    lateinit var wifiAutoConnect: WifiAutoConnect

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
//                 viewModel.askedWifiPermissions.set(true)
            }
            .observe(this) {
                viewModel.onNetworkStatusChanged(
                    NetworkOptions(
                        it.isAvailable, isWifiConnected(), getWifiName()
                    )
                )
                //                AutoRetryHandler.value = it.isAvailable
                /*val isUnknownOrKnownWifiConnection =
                    isUnknownOrKnownWifiConnection(ACCESS_POINT_SSID)
                if (!viewModel.isWifiDialogShowing && !isUnknownOrKnownWifiConnection) {
                    setIsWifiDialogShowing(true)
                    navController.navigate(WifiFragmentDirections.toWifiFragment(true))
                }*/
            }
            .start()

        reactToTask(viewModel.combinedLiveTask)
        viewModel.transferFileProgress.observe(this) {
            print("transfer")
            println(it)
        }
        viewModel.networkStatusCheckerLiveTask.asLiveData().observe(this) {
            println(it.result())
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        networkStatusObserver.stop()
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
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
import com.thanosfisherman.wifiutils.WifiUtils
import com.thanosfisherman.wifiutils.wifiConnect.ConnectionErrorCode
import com.thanosfisherman.wifiutils.wifiConnect.ConnectionSuccessListener
import com.viam.feeder.R
import com.viam.feeder.constants.ACCESS_POINT_PASSWORD
import com.viam.feeder.constants.ACCESS_POINT_SSID
import com.viam.feeder.core.utility.bindingAdapter.contentView
import com.viam.feeder.core.utility.reactToTask
import com.viam.feeder.databinding.ActivityMainBinding
import com.viam.feeder.ui.wifi.Connectivity.isUnknownOrKnownWifiConnection
import com.viam.feeder.ui.wifi.NetworkStatusObserver
import com.viam.feeder.ui.wifi.WifiAutoConnect
import com.viam.feeder.ui.wifi.WifiFragmentDirections
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

        WifiUtils.withContext(applicationContext)
            .connectWith(ACCESS_POINT_SSID, ACCESS_POINT_PASSWORD)
            .setTimeout(40000)
            .onConnectionResult(object : ConnectionSuccessListener {
                override fun success() {
                    Toast.makeText(this@MainActivity, "SUCCESS!", LENGTH_SHORT).show()
                }

                override fun failed(errorCode: ConnectionErrorCode) {
                    Toast.makeText(
                        this@MainActivity,
                        "EPIC FAIL!$errorCode",
                        LENGTH_SHORT
                    ).show()
                }
            })
            .start()

        setupViews()
        networkStatusObserver
            .withActivity(this)
            .onPermissionCallback {
                viewModel.askedWifiPermissions.set(true)
            }
            .observe(this) {
//                AutoRetryHandler.value = it.isAvailable
                val isUnknownOrKnownWifiConnection =
                    isUnknownOrKnownWifiConnection(ACCESS_POINT_SSID)
                if (!viewModel.isWifiDialogShowing && !isUnknownOrKnownWifiConnection) {
                    setIsWifiDialogShowing(true)
                    navController.navigate(WifiFragmentDirections.toWifiFragment(true))
                }
            }
            .start()

        /*  TaskEventLogger.events.observe(this, EventObserver { resource ->
              resource?.onError {
                  Timber.e(it)
                  if (it.isConnectionError() && !viewModel.isWifiDialogShowing && viewModel.askedWifiPermissions.get()) {
                      setIsWifiDialogShowing(true)
                      navController.navigate(WifiFragmentDirections.toWifiFragment(false))
                  } else {
                      showMessage(it.toMessage(this))
                  }
              }
          })*/
        reactToTask(viewModel.getConfigTask)
        viewModel.transferFileProgress.observe(this) {
            print("transfer")
            println(it)
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
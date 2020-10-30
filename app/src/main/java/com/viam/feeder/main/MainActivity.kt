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
import com.viam.feeder.R
import com.viam.feeder.core.livedata.EventObserver
import com.viam.feeder.core.network.NetworkStatus
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.content_main.bottom_nav

@AndroidEntryPoint

class MainActivity : AppCompatActivity() {
    private val viewModel: MainViewModel by viewModels()

    lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)
        setupViews()
        viewModel.networkStatus.connection.observe(this, {
            if (!viewModel.networkStatus.isShowing && it != NetworkStatus.CONNECTION_STATE_SUCCESS) {
//                navController.navigate(R.id.wifi_fragment)
            }
        })
        viewModel.globalRequest.errorEvents.observe(this, EventObserver {
            Toast.makeText(this, it, Toast.LENGTH_SHORT).show()
        })

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

    private var backPressedOnce = false

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
}
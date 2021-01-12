package com.viam.feeder

import androidx.multidex.MultiDexApplication
import com.facebook.flipper.android.AndroidFlipperClient
import com.facebook.flipper.android.utils.FlipperUtils
import com.facebook.flipper.plugins.network.NetworkFlipperPlugin
import com.facebook.soloader.SoLoader
import com.google.firebase.analytics.FirebaseAnalytics
import dagger.hilt.android.HiltAndroidApp
import timber.log.Timber
import javax.inject.Inject

@HiltAndroidApp
class MyApplication : MultiDexApplication() {
    @Inject
    lateinit var networkFlipperPlugin: NetworkFlipperPlugin

    companion object {
        var mFirebaseAnalytics: FirebaseAnalytics? = null
    }

    override fun onCreate() {
        super.onCreate()
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);
        setupDebuggingTools()
    }

    private fun setupDebuggingTools() {
        Timber.plant(Timber.DebugTree())

        SoLoader.init(this, false)

        if (FlipperUtils.shouldEnableFlipper(this)) {
            AndroidFlipperClient.getInstance(this)
                .apply {
                    addPlugin(networkFlipperPlugin)
                }
                .start()
        }
    }
}
package com.viam.feeder

import android.app.Application
import com.google.firebase.analytics.FirebaseAnalytics
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class MyApplication : Application() {

    companion object {
        lateinit var context: Application
        var mFirebaseAnalytics: FirebaseAnalytics? = null

    }

    override fun onCreate() {
        super.onCreate()
        context = this
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);
    }
}
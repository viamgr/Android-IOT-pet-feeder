package com.viam.feeder

import android.app.Application
import android.widget.Toast
import androidx.multidex.MultiDexApplication
import com.google.firebase.analytics.FirebaseAnalytics
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

@HiltAndroidApp
class MyApplication : MultiDexApplication() {

    companion object {
        fun toast(value: String) {
            GlobalScope.launch(Dispatchers.Main) {
                Toast.makeText(context, "value:$value", Toast.LENGTH_SHORT).show()
            }

        }

        lateinit var context: Application
        var mFirebaseAnalytics: FirebaseAnalytics? = null

    }

    override fun onCreate() {
        super.onCreate()
        context = this
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);
    }
}
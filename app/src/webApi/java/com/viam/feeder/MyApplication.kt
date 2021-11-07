package com.viam.feeder

import androidx.multidex.MultiDexApplication
import com.facebook.flipper.android.AndroidFlipperClient
import com.facebook.flipper.android.utils.FlipperUtils
import com.facebook.flipper.plugins.network.NetworkFlipperPlugin
import com.facebook.soloader.SoLoader
import com.google.firebase.analytics.FirebaseAnalytics
import com.part.binidng.views.addDefaultClassicViewTypes
import com.part.livetaskcore.LiveTaskManager
import com.part.livetaskcore.Resource
import com.part.livetaskcore.connection.MultipleConnectionInformer
import com.part.livetaskcore.livatask.CombinedException
import com.viam.feeder.core.utils.toMessage
import com.viam.websocket.WebSocketApi
import dagger.hilt.android.HiltAndroidApp
import timber.log.Timber
import javax.inject.Inject
import com.viam.resource.Resource as AppResource

@HiltAndroidApp
class MyApplication : MultiDexApplication() {
    @Inject
    lateinit var networkFlipperPlugin: NetworkFlipperPlugin

    @Inject
    lateinit var webSocketApi: WebSocketApi

    @Inject
    lateinit var liveTaskManager: LiveTaskManager

    @Inject
    lateinit var multipleConnectionInformer: MultipleConnectionInformer

    companion object {
        var mFirebaseAnalytics: FirebaseAnalytics? = null
    }

    override fun onCreate() {
        super.onCreate()
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this)
        setupDebuggingTools()
        setupLiveTask()
    }

    private fun setupLiveTask() {
        println("multipleConnectionInformer:${multipleConnectionInformer.hashCode()}")
        liveTaskManager.Builder()
            .setConnectionInformer(multipleConnectionInformer)
            .setErrorMapper { exception ->
                println("livetask setErrorMapper exception")
                if (exception is CombinedException) {
                    exception.exceptions.forEach {
                        it.printStackTrace()
                    }
                }
                exception.printStackTrace()
                Exception(
                    exception.toMessage(this@MyApplication),
                    exception
                )
            }
            .setResourceMapper {
                if (it is AppResource<*>) {
                    it.toResource()
                } else {
                    Resource.Success(it)
                }
            }
            .addDefaultClassicViewTypes()

            .apply()
    }

    private fun AppResource<*>.toResource(): Resource<*> {
        return when (this) {
            is AppResource.Success -> {
                Resource.Success(data)
            }
            is AppResource.Error -> {
                Resource.Error(exception)
            }
            is AppResource.Loading -> {
                Resource.Loading(this.data)
            }
        }
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
package com.viam.feeder.broadcast

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.lifecycle.Lifecycle.Event.ON_DESTROY
import androidx.lifecycle.Lifecycle.Event.ON_START
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.OnLifecycleEvent
import android.content.BroadcastReceiver as StockReceiver

/** @author Aidan Follestad (@afollestad) */
class BroadcastReceiver<T>(
    context: T,
    constructor: Builder.() -> Unit
) : LifecycleObserver where T : Context, T : LifecycleOwner {

    private val appContext = context.applicationContext
    private val filter: IntentFilter
    private val instructions: List<Instructions>

    init {
        val builder = Builder()
        constructor(builder)
        filter = builder.filter()
        instructions = builder.instructions()

        context.lifecycle.addObserver(this)
    }

    private val broadcastReceiver = object : StockReceiver() {
        override fun onReceive(
            context: Context,
            intent: Intent
        ) {
            for (ins in instructions) {
                if (ins.matches(intent)) {
                    ins.execution().invoke(intent)
                    break
                }
            }
        }
    }

    @OnLifecycleEvent(ON_START)
    fun start() {
        appContext.registerReceiver(broadcastReceiver, filter)
    }

    @OnLifecycleEvent(ON_DESTROY)
    fun stop() = appContext.unregisterReceiver(broadcastReceiver)
}
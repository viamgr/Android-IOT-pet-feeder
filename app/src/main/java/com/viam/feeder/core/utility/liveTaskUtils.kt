package com.viam.feeder.core.utility

import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.part.binidng.bindingadapter.reactToTask
import com.part.livetaskcore.livatask.LiveTask

fun Fragment.reactToTask(
    liveTask: LiveTask<*>,
    targetView: View = activity!!.window.decorView.findViewById(android.R.id.content)
) {
    targetView.reactToTask(liveTask, viewLifecycleOwner)
}

fun AppCompatActivity.reactToTask(
    liveTask: LiveTask<*>,
    targetView: View = window.decorView.rootView.findViewById(android.R.id.content)
) {
    targetView.reactToTask(liveTask, this)
}
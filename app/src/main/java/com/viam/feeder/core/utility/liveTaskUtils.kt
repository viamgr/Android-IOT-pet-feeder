package com.viam.feeder.core.utility

import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.part.livetaskcore.bindingadapter.reactToTask
import com.part.livetaskcore.livatask.LiveTask

fun Fragment.reactToTask(
    liveTask: LiveTask<*>,
    targetView: View = activity!!.window.decorView
) {
    targetView.reactToTask(liveTask, viewLifecycleOwner)
}


fun AppCompatActivity.reactToTask(
    liveTask: LiveTask<*>,
    targetView: View = window.decorView
) {
    targetView.reactToTask(liveTask, this)
}
package com.viam.feeder.core.utility

import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.part.binidng.bindingadapter.reactToTask
import com.part.livetaskcore.livatask.LiveTask
import com.part.livetaskcore.livatask.LoadingMessageBlock

fun Fragment.reactToTask(
    liveTask: LiveTask<*>,
    loadingMessageBlock: LoadingMessageBlock? = null,
    targetView: View = activity!!.window.decorView.findViewById(android.R.id.content)
) {
    targetView.reactToTask(liveTask, loadingMessageBlock, viewLifecycleOwner)
}

fun AppCompatActivity.reactToTask(
    liveTask: LiveTask<*>,
    loadingMessageBlock: LoadingMessageBlock? = null,

    targetView: View = window.decorView.rootView.findViewById(android.R.id.content)
) {
    targetView.reactToTask(
        liveTask,
        loadingMessageBlock = loadingMessageBlock,
        viewLifecycleOwner = this
    )
}
package com.part.binidng.bindingadapter

import android.view.View
import android.widget.Button
import androidx.core.view.isVisible
import androidx.databinding.BindingAdapter
import androidx.lifecycle.LifecycleOwner
import com.part.binidng.views.ClassicViewTypeHandler
import com.part.livetaskcore.LiveTaskManager
import com.part.livetaskcore.Resource
import com.part.livetaskcore.livatask.LiveTask
import com.part.livetaskcore.livatask.LoadingMessageBlock
import com.part.livetaskcore.views.*

@OptIn(ExperimentalStdlibApi::class)
@BindingAdapter(
    value = ["reactToTask", "result", "loadingMessageBlock", "liveTaskManager"],
    requireAll = false
)
fun View.reactToTask(
    liveTask: LiveTask<*>?,
    result: Resource<*>?,
    loadingMessageBlock: LoadingMessageBlock? = null,
    liveTaskManager: LiveTaskManager? = LiveTaskManager.instance
) {
    println("reactToTask")
    val taskManager = liveTaskManager ?: LiveTaskManager.instance
    taskManager.apply {
        findViewTypeHandler<ClassicViewTypeHandler>(liveTask?.loadingViewType()).onUpdate(
            liveTask,
            result ?: liveTask?.result(),
            loadingMessageBlock = loadingMessageBlock ?: liveTask?.loadingMessage(),
            this@reactToTask
        )
    }
}

fun View.reactToTask(
    liveTask: LiveTask<*>,
    loadingMessageBlock: LoadingMessageBlock? = null,
    viewLifecycleOwner: LifecycleOwner
) {
    liveTask.asLiveData().observe(viewLifecycleOwner) {
        reactToTask(it, it.result(), loadingMessageBlock = loadingMessageBlock)
    }
}

@BindingAdapter("visibleOnLoading")
fun View.visibleOnLoading(liveTask: LiveTask<*>?) {
    this.isVisible = liveTask?.result() is Resource.Loading
}

@BindingAdapter("disableOnLoading")
fun Button.disableOnLoading(liveTask: LiveTask<*>?) {
    this.isEnabled = liveTask?.result() is Resource.Loading
}
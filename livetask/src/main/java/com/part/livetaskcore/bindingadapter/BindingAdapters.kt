package com.part.livetaskcore.bindingadapter

import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.core.view.isVisible
import androidx.databinding.BindingAdapter
import androidx.lifecycle.LifecycleOwner
import com.part.livetaskcore.livatask.LiveTask
import com.viam.resource.Resource

@OptIn(ExperimentalStdlibApi::class)
@BindingAdapter(value = ["reactToTask", "type"], requireAll = false)
fun View.reactToTask(
    liveTask: LiveTask<*>?,
    progressType: ProgressType?
) {
    print("reactToTask")
    println(liveTask?.result())
    val loadingViewType = liveTask?.loadingViewType
    liveTask?.result()?.let {
        val viewParent = getViewParent(this, progressType, loadingViewType)
        val state = (progressType ?: loadingViewType).getState()
        when (it) {
            is Resource.Success -> {
                state.success(viewParent.view, viewParent.parent as ViewGroup, liveTask, this)
            }
            is Resource.Loading -> {
                state.loading(viewParent.view, viewParent.parent as ViewGroup, liveTask, this)

            }
            is Resource.Error -> {
                state.error(viewParent.view, viewParent.parent as ViewGroup, liveTask, this)
            }
        }
    }
}

fun View.reactToTask(liveTask: LiveTask<*>, viewLifecycleOwner: LifecycleOwner) {
    liveTask.asLiveData().observe(viewLifecycleOwner) {
        reactToTask(it, it.loadingViewType)
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
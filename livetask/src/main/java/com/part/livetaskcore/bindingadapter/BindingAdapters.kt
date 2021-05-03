package com.part.livetaskcore.bindingadapter

import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.core.view.isVisible
import androidx.databinding.BindingAdapter
import androidx.lifecycle.LifecycleOwner
import com.part.livetaskcore.Resource
import com.part.livetaskcore.livatask.LiveTask

@OptIn(ExperimentalStdlibApi::class)
@BindingAdapter(value = ["reactToTask"], requireAll = false)
fun View.reactToTask(
    liveTask: LiveTask<*>?,
) {
    print("reactToTask")
    println(liveTask?.result())
    val loadingViewType = liveTask?.loadingViewType ?: CircularViewType()
    liveTask?.result()?.let {
        val viewParent = getViewParent(this, loadingViewType)
        when (it) {
            is Resource.Success -> {
                loadingViewType.success(
                    viewParent.view,
                    viewParent.parent as ViewGroup,
                    liveTask,
                    this
                )
            }
            is Resource.Loading -> {
                loadingViewType.loading(
                    viewParent.view,
                    viewParent.parent as ViewGroup,
                    liveTask,
                    this
                )

            }
            is Resource.Error -> {
                loadingViewType.error(
                    viewParent.view,
                    viewParent.parent as ViewGroup,
                    liveTask,
                    this
                )
            }
        }
    }

}

fun View.reactToTask(liveTask: LiveTask<*>, viewLifecycleOwner: LifecycleOwner) {
    liveTask.asLiveData().observe(viewLifecycleOwner) {
        reactToTask(it)
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
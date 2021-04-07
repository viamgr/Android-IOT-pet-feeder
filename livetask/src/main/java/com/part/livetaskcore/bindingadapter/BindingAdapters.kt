package com.part.livetaskcore.bindingadapter

import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.annotation.LayoutRes
import androidx.core.view.isVisible
import androidx.databinding.BindingAdapter
import com.part.livetaskcore.livatask.LiveTask
import com.viam.resource.Resource

@OptIn(ExperimentalStdlibApi::class)
@BindingAdapter(value = ["reactToTask", "type", "layout"], requireAll = false)
fun View.reactToTask(
    liveTask: LiveTask<*>?,
    progressType: ProgressType?,
    @LayoutRes layout: Int?,
) {
    val loadingViewType = liveTask?.loadingViewType
    when (liveTask?.result()) {
        is Resource.Success -> {
            val viewParent = getViewParent(this, progressType, layout, loadingViewType)

            if (layout == null) {
                (progressType ?: loadingViewType).getState()
                    .success(viewParent.view, viewParent.parent as ViewGroup, liveTask, this)
            }
        }
        is Resource.Loading -> {
            val viewParent = getViewParent(this, progressType, layout, loadingViewType)
            if (layout == null) {
                (progressType ?: loadingViewType).getState()
                    .loading(viewParent.view, viewParent.parent as ViewGroup, liveTask, this)
            }
        }
        is Resource.Error -> {
            val viewParent = getViewParent(this, progressType, layout, loadingViewType)
            if (layout == null) {
                (progressType ?: loadingViewType).getState()
                    .error(viewParent.view, viewParent.parent as ViewGroup, liveTask, this)
            }
        }
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
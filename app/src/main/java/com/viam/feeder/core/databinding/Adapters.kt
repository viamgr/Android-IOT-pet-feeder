package com.viam.feeder.core.databinding

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.core.view.isVisible
import androidx.databinding.BindingAdapter
import com.google.android.material.button.MaterialButton
import com.viam.feeder.R
import com.viam.feeder.core.Resource
import com.viam.feeder.core.isError
import com.viam.feeder.core.isLoading
import com.viam.feeder.core.task.CompositeException
import kotlinx.android.synthetic.main.fragment_loading.view.*


@BindingAdapter("goneUnless")
fun View.goneUnless(visible: Boolean) {
    visibility = if (visible) View.VISIBLE else View.GONE
}


@BindingAdapter("backgroundTint")
fun Button.backgroundTint(color: Int) {
    setBackgroundColor(color)
}

@BindingAdapter("icon")
fun MaterialButton.setIcon(icon: Int) {
    setIconResource(icon)
}

@BindingAdapter("visibleOnLoading")
fun View.visibleOnLoading(resource: Resource<*>?) {
    isVisible = resource?.isLoading() ?: false
}

@BindingAdapter("requestResource", "retryClicked", "dismissClicked", requireAll = true)
fun ViewGroup.requestResource(
    requestResource: Resource<Any>?,
    retryClicked: () -> Unit,
    dismissClicked: () -> Unit
) {
    if (loading_root == null) {
        LayoutInflater.from(context).inflate(R.layout.fragment_loading, this)
    }
    loading_root.isVisible =
        requestResource?.isLoading() ?: false || requestResource?.isError() ?: false

    progress.isVisible = requestResource?.isLoading() ?: false
    error_group.isVisible = requestResource?.isError() ?: false
    retry.setOnClickListener {
        retryClicked()
    }
    close.setOnClickListener {
        dismissClicked()
    }
    if (requestResource is Resource.Error) {
        //todo Parse error message function
        val exception = requestResource.exception
        if (exception is CompositeException) {
            error.text = exception.errors.joinToString("\n") { it.message.toString() }
        } else {
            error.text = exception.message
        }

    }
}

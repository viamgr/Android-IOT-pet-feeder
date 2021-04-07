package com.viam.feeder.core.databinding

import android.graphics.drawable.Drawable
import android.view.View
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.ImageView
import androidx.annotation.StringRes
import androidx.core.view.isVisible
import androidx.databinding.BindingAdapter
import coil.load
import com.google.android.material.button.MaterialButton
import com.viam.resource.Resource
import com.viam.resource.isLoading


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


@BindingAdapter("imageUrl")
fun ImageView.imageUrl(resource: Drawable) {
    load(resource)
}

@BindingAdapter("noFilterText")
fun AutoCompleteTextView.noFilterText(@StringRes stringRes: Int) {
    setText(context.getString(stringRes), false)
}

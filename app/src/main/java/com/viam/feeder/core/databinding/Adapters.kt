package com.viam.feeder.core.databinding

import android.view.View
import android.widget.Button
import androidx.databinding.BindingAdapter
import com.google.android.material.button.MaterialButton

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
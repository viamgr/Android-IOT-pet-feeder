package com.viam.feeder.core.databinding

import android.annotation.SuppressLint
import android.graphics.PorterDuff
import android.graphics.drawable.Drawable
import android.os.Build
import android.widget.ImageView
import androidx.annotation.ColorInt
import androidx.core.graphics.drawable.DrawableCompat
import androidx.databinding.BindingAdapter
import androidx.vectordrawable.graphics.drawable.VectorDrawableCompat


@SuppressLint("NewApi")
@BindingAdapter("tintCompat")
fun setTintCompat(imageView: ImageView, @ColorInt color: Int) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
        imageView.imageTintList =
            androidx.databinding.adapters.Converters.convertColorToColorStateList(
                color
            )
    } else {
        val originalDrawable: Drawable = imageView.drawable
        if (originalDrawable is VectorDrawableCompat) {
            originalDrawable.setTint(color)
        } else {
            val tintedDrawable = tintDrawable(originalDrawable, color)
            imageView.setImageDrawable(tintedDrawable)
        }
    }
}

fun tintDrawable(drawable: Drawable, @ColorInt tintInt: Int): Drawable {
    var drawable = drawable
    drawable = DrawableCompat.wrap(DrawableCompat.unwrap<Drawable>(drawable).mutate())
    DrawableCompat.setTintMode(drawable, PorterDuff.Mode.SRC_IN)
    DrawableCompat.setTint(drawable, tintInt)
    return drawable
}
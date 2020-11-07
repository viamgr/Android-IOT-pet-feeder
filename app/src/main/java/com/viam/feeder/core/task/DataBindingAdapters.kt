package com.viam.feeder.core.task

import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.view.ViewCompat
import androidx.core.view.isVisible
import androidx.databinding.BindingAdapter
import com.viam.feeder.R
import com.viam.feeder.core.Resource
import com.viam.feeder.core.domain.toMessage
import com.viam.feeder.core.isError
import com.viam.feeder.core.isLoading
import eightbitlab.com.blurview.BlurView
import eightbitlab.com.blurview.RenderScriptBlur
import java.util.concurrent.CancellationException


@BindingAdapter("taskProgress")
fun View.taskProgress(liveTask: LiveTask<*, *>?) {
    val state = liveTask?.state()
    val isLoading = state?.isLoading() == true
    val showError = state is Resource.Error && state.exception !is CancellationException
    val isVisible = isLoading || showError

    val parent = if (parent !is ConstraintLayout && this is ConstraintLayout) {
        this
    } else {
        parent as ViewGroup
    }
    if (parent.id == -1) {
        parent.id = ViewCompat.generateViewId()
    }

    if (!isVisible) {
        if (tag != null) {
            parent.removeView(parent.findViewById(tag as Int))
        }
    } else {
        val viewId = id
        var isInflated = false

        val view = if (parent is ConstraintLayout) {
            val inflated = tag?.let {
                (parent).findViewById(it as Int)
            } ?: run {
                isInflated = true
                View.inflate(context, R.layout.layout_row_progress, null).let {
                    ViewCompat.setElevation(it, Float.MAX_VALUE)
                    parent.addView(it)
                    it
                }
            }
            if (isInflated) {
                val generateViewId = ViewCompat.generateViewId()
                inflated.id = generateViewId
                tag = generateViewId
                val set = ConstraintSet()
                set.connect(inflated.id, ConstraintSet.TOP, viewId, ConstraintSet.TOP, 0)
                set.connect(inflated.id, ConstraintSet.START, viewId, ConstraintSet.START, 0)
                set.connect(inflated.id, ConstraintSet.END, viewId, ConstraintSet.END, 0)
                set.connect(inflated.id, ConstraintSet.BOTTOM, viewId, ConstraintSet.BOTTOM, 0)
                set.applyTo(parent)
            }

            inflated
        } else {
            isInflated = true
            View.inflate(context, R.layout.layout_row_progress, parent)
        }

        val errorView = view.findViewById<TextView>(R.id.error)
        val retryView = view.findViewById<View>(R.id.retry)
        val closeView = view.findViewById<View>(R.id.close)

        view.findViewById<View>(R.id.progress).isVisible = isLoading
        closeView.isVisible = liveTask?.isCancelable() == true
        retryView.isVisible = state?.isError() == true
        if (state is Resource.Error) {
            errorView.text = state.exception.toMessage(context)
        } else if (state is Resource.Loading) {
            errorView.text = context.getString(R.string.loading)
        }

        if (isInflated) {
            val blurView = view.findViewById<BlurView>(R.id.blurView)
            blurView.setupWith(this as ViewGroup)
                .setBlurAlgorithm(RenderScriptBlur(context))
                .setBlurRadius(2F)
                .setBlurAutoUpdate(true)
                .setHasFixedTransformationMatrix(true)

            retryView.setOnClickListener {
                liveTask?.retry()
            }
            closeView.setOnClickListener {
                liveTask?.cancel()
            }
        }

    }
}
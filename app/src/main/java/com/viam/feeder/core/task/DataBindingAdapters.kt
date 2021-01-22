package com.viam.feeder.core.task

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.TextView
import androidx.annotation.LayoutRes
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.view.ViewCompat
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import androidx.core.view.postDelayed
import androidx.databinding.BindingAdapter
import com.viam.feeder.R
import com.viam.feeder.core.Resource
import com.viam.feeder.core.domain.utils.toMessage
import com.viam.feeder.core.isError
import com.viam.feeder.core.isLoading
import eightbitlab.com.blurview.BlurView
import eightbitlab.com.blurview.RenderScriptBlur
import java.util.concurrent.CancellationException


@BindingAdapter(
    value = ["disableClicksOnLoading"],
    requireAll = true
)
fun View.disableClicksOnLoading(
    resource: Resource<*>?,
) {
    if (resource?.isLoading() == true && tag !is Int) {
        val frameLayout = FrameLayout(context).also { frameLayout ->
            frameLayout.isClickable = true
            frameLayout.id = ViewCompat.generateViewId().also {
                tag = it
            }
            ViewCompat.setElevation(frameLayout, Float.MAX_VALUE)
        }
        (this as ViewGroup).addView(frameLayout, measuredWidth, height)
    } else {
        if (tag is Int) {
            findViewById<View?>(tag as Int)?.let {
                tag = null
                (this as ViewGroup).removeView(it)
            }
        }
    }
}

@BindingAdapter(
    value = ["animationResource", "doneAnimationLayout", "doneAnimationDelay"],
    requireAll = true
)
fun View.doneAnimationLayout(
    resource: Resource<*>?,
    @LayoutRes doneAnimationLayout: Int?,
    doneAnimationDelay: Long?
) {
    if (resource is Resource.Success && doneAnimationLayout != null) {
        val inflater = LayoutInflater.from(context)
        val viewGroup = if (parent !is ConstraintLayout && this is ConstraintLayout) {
            this
        } else if (this is FrameLayout) {
            this
        } else {
            parent as ViewGroup
        }
        inflater.inflate(doneAnimationLayout, viewGroup, false).let { doneView ->
            ViewCompat.setElevation(doneView, Float.MAX_VALUE)
            doneView.layoutParams =
                ViewGroup.LayoutParams(viewGroup.measuredWidth, viewGroup.height)
            viewGroup.addView(doneView)

            viewGroup.postDelayed(doneAnimationDelay ?: 2500L) {
                viewGroup.removeView(doneView)
            }
        }
    }

}

@BindingAdapter(
    value = ["taskProgress"],
    requireAll = true
)
fun View.taskProgress(
    liveTask: LiveTask<*, *>?
) {
    val state = liveTask?.state()
    val isLoading = state?.isLoading() == true
    val showError = state is Resource.Error && state.exception !is CancellationException
    val shouldVisible = isLoading || showError

    val targetView =
        when {
            parent !is ConstraintLayout && this is ConstraintLayout -> {
                this
            }
            this is FrameLayout -> {
                this
            }
            else -> {
                parent as ViewGroup
            }
        }
    if (targetView.id == -1) {
        targetView.id = ViewCompat.generateViewId()
    }

    if (!shouldVisible) {
        if (tag != null) {
            targetView.removeView(targetView.findViewById(tag as Int))
            tag = -1
        }
    } else {
        val viewId = id
        val isNew = tag == null || tag == -1

        val workingView = when {
            tag != null && tag != -1 -> {
                (targetView).findViewById(tag as Int)
            }
            targetView is ConstraintLayout -> {
                View.inflate(context, R.layout.layout_row_progress, null).let {
                    ViewCompat.setElevation(it, Float.MAX_VALUE)
                    targetView.addView(it)
                    val generateViewId = ViewCompat.generateViewId()
                    it.id = generateViewId
                    tag = generateViewId
                    val set = ConstraintSet()
                    set.connect(it.id, ConstraintSet.TOP, viewId, ConstraintSet.TOP, 0)
                    set.connect(it.id, ConstraintSet.START, viewId, ConstraintSet.START, 0)
                    set.connect(it.id, ConstraintSet.END, viewId, ConstraintSet.END, 0)
                    set.connect(it.id, ConstraintSet.BOTTOM, viewId, ConstraintSet.BOTTOM, 0)
                    set.applyTo(targetView)
                    it
                }
            }
            else -> {
                View.inflate(context, R.layout.layout_row_progress, null).let {
                    ViewCompat.setElevation(it, Float.MAX_VALUE)
                    targetView.addView(it)
                    if (targetView is FrameLayout)
                        it.layoutParams =
                            FrameLayout.LayoutParams(targetView.measuredWidth, targetView.height)

                    val generateViewId = ViewCompat.generateViewId()
                    it.id = generateViewId
                    tag = generateViewId
                    it
                }

            }
        }

//        view.layoutParams = parent.layoutParams

        val errorView = workingView.findViewById<TextView>(R.id.error)
        val retryView = workingView.findViewById<View>(R.id.retry)
        val closeView = workingView.findViewById<View>(R.id.close)

        workingView.findViewById<View>(R.id.progress).isInvisible = !isLoading
        closeView.isVisible = liveTask?.isCancelable() == true
        retryView.isVisible = state?.isError() == true
        if (state is Resource.Error) {
            errorView.text = state.exception.toMessage(context)
        } else if (state is Resource.Loading) {
            errorView.text = context.getString(R.string.loading)
        }

        if (isNew) {
            val blurView = workingView.findViewById<BlurView>(R.id.blurView)
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
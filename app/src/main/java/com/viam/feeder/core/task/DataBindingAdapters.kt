package com.viam.feeder.core.task

import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import androidx.annotation.LayoutRes
import androidx.constraintlayout.widget.ConstraintLayout
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
import com.viam.feeder.core.isSuccess
import eightbitlab.com.blurview.BlurView
import eightbitlab.com.blurview.RenderScriptBlur
import java.util.concurrent.CancellationException


const val disableClicksOnLoadingTag = R.id.disableClicksOnLoadingTag

@BindingAdapter(
    value = ["disableClicksOnLoading"],
    requireAll = true
)
fun View.disableClicksOnLoading(
    resource: Resource<*>?,
) {
    val tag = getTag(disableClicksOnLoadingTag)
    if (resource?.isLoading() == true && tag !is Int) {
        val frameLayout = FrameLayout(context).also { frameLayout ->
            frameLayout.isClickable = true
            frameLayout.id = ViewCompat.generateViewId().also {
                setTag(disableClicksOnLoadingTag, it)
            }
            ViewCompat.setElevation(frameLayout, Float.MAX_VALUE / 2)

            val outValue = TypedValue()
            context.theme
                .resolveAttribute(android.R.attr.selectableItemBackground, outValue, true)
            frameLayout.setBackgroundResource(outValue.resourceId)
        }
        (this as ViewGroup).addView(frameLayout, measuredWidth, height)
    } else {
        if (tag is Int) {
            findViewById<View?>(tag)?.let {
                setTag(disableClicksOnLoadingTag, null)
                (this as ViewGroup).removeView(it)
            }
        }
    }
}

const val doneAnimationLayoutTag = R.id.doneAnimationLayoutTag

@BindingAdapter(
    value = ["animationResource", "doneAnimationLayout", "doneAnimationDelay"],
    requireAll = true
)
fun View.doneAnimationLayout(
    resource: Resource<*>?,
    @LayoutRes doneAnimationLayout: Int,
    doneAnimationDelay: Long?
) {
    val alreadyVisible = getTag(doneAnimationLayoutTag) == false
    if (resource?.isSuccess() == true && !alreadyVisible) {
        setTag(doneAnimationLayoutTag, false)
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
    } else {
        setTag(doneAnimationLayoutTag, true)
    }

}

const val taskProgressLayoutTag = R.id.taskProgressLayoutTag

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
        when (this) {
            is LinearLayout, !is ViewGroup -> {
                parent as ViewGroup
            }
            else -> {
                this
            }
        }
    if (targetView.id == -1) {
        targetView.id = ViewCompat.generateViewId()
    }

    val viewTag = getTag(taskProgressLayoutTag)
    if (!shouldVisible) {
        if (viewTag != null) {
            targetView.removeView(targetView.findViewById(viewTag as Int))
            setTag(taskProgressLayoutTag, null)
        }
    } else {
        val isNew = viewTag !is Int

        val workingView = when {
            !isNew -> {
                (targetView).findViewById(viewTag as Int)
            }
            else -> {
                View.inflate(context, R.layout.layout_row_progress, null).let {
                    ViewCompat.setElevation(it, Float.MAX_VALUE)
                    val generateViewId = ViewCompat.generateViewId()
                    it.id = generateViewId
                    setTag(taskProgressLayoutTag, generateViewId)

                        targetView.addView(it, targetView.measuredWidth, targetView.measuredHeight)

                    it
                }

            }
        }

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
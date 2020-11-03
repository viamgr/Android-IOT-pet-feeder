package com.viam.feeder.core.task

import android.view.LayoutInflater
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
import com.viam.feeder.core.isError
import com.viam.feeder.core.isLoading
import eightbitlab.com.blurview.BlurView
import eightbitlab.com.blurview.RenderScriptBlur
import kotlinx.android.synthetic.main.fragment_loading.view.*
import java.util.concurrent.CancellationException


@BindingAdapter("taskProgress")
fun View.taskProgress(request: PromiseTask<*, *>?) {
    val result = request?.status()
    val isLoading = result?.isLoading() == true
    val showError = result is Resource.Error && result.exception !is CancellationException
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
        val view = if (parent is ConstraintLayout) {
            val inflated = tag?.let {
                (parent).findViewById(it as Int)
            } ?: run {
                val generateViewId = ViewCompat.generateViewId()
                tag = generateViewId
                View.inflate(context, R.layout.layout_row_progress, null).let {
                    it.id = generateViewId
                    parent.addView(it)
                    it
                }
            }
            val set = ConstraintSet()
            set.connect(inflated.id, ConstraintSet.TOP, viewId, ConstraintSet.TOP, 0)
            set.connect(inflated.id, ConstraintSet.START, viewId, ConstraintSet.START, 0)
            set.connect(inflated.id, ConstraintSet.END, viewId, ConstraintSet.END, 0)
            set.connect(inflated.id, ConstraintSet.BOTTOM, viewId, ConstraintSet.BOTTOM, 0)
            set.applyTo(parent)

            inflated
        } else {
            View.inflate(context, R.layout.layout_row_progress, parent as ViewGroup)
        }

        val blurView = view.findViewById<BlurView>(R.id.blurView)
        val errorView = view.findViewById<TextView>(R.id.error)
        val retryView = view.findViewById<View>(R.id.retry)
        val closeView = view.findViewById<View>(R.id.close)
        errorView?.post {
            view.findViewById<View>(R.id.progress).isVisible = isLoading
        }
        retryView.isVisible = result?.isError() == true
        if (result is Resource.Error) {
            //todo Parse error message function
            val exception = result.exception
            if (exception is CompositeException) {
                errorView.text = exception.errors.joinToString("\n") { it.message.toString() }
            } else {
                errorView.text = exception.message
            }
        } else if (result is Resource.Loading) {
            errorView.text = context.getString(R.string.loading)
        }
        blurView.setupWith(this as ViewGroup)
            .setBlurAlgorithm(RenderScriptBlur(context))
            .setBlurRadius(2F)
            .setBlurAutoUpdate(true)
            .setHasFixedTransformationMatrix(true)

        retryView.setOnClickListener {
            request?.retry()
        }
        closeView.setOnClickListener {
            request?.cancel()
        }

    }


}

@BindingAdapter("promiseRequest", requireAll = true)
fun ViewGroup.promiseTask(request: PromiseTask<*, *>?) {
    val loadingRoot =
        if (getChildAt(childCount - 1) == null || getChildAt(childCount - 1).id != R.id.loading_root) {
            LayoutInflater.from(context).inflate(R.layout.fragment_loading, this)
            getChildAt(childCount - 1)
        } else {
            getChildAt(childCount - 1)
        }
    val result = request?.status()
    loadingRoot.isVisible =
        result?.isLoading() ?: false || (result is Resource.Error && result.exception !is CancellationException)

    progress.isVisible = result?.isLoading() ?: false
    error_group.isVisible = result?.isError() ?: false
    retry.setOnClickListener {
        request?.retry()
    }
    close.setOnClickListener {
        request?.cancel()
    }
    if (result is Resource.Error) {
        //todo Parse error message function
        val exception = result.exception
        if (exception is CompositeException) {
            error.text = exception.errors.joinToString("\n") { it.message.toString() }
        } else {
            error.text = exception.message
        }
    }
}

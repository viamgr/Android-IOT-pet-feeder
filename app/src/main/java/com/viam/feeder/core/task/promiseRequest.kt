package com.viam.feeder.core.task

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.databinding.BindingAdapter
import com.viam.feeder.R
import com.viam.feeder.core.Resource
import com.viam.feeder.core.isError
import com.viam.feeder.core.isLoading
import kotlinx.android.synthetic.main.fragment_loading.view.*
import java.util.concurrent.CancellationException

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

package com.part.binidng.views

import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import com.part.binidng.R
import com.part.livetaskcore.Resource
import com.part.livetaskcore.livatask.LiveTask
import com.part.livetaskcore.views.getErrorText
import kotlinx.android.synthetic.main.loading_circular.view.cl_container_circular
import kotlinx.android.synthetic.main.loading_circular.view.cl_error_circular
import kotlinx.android.synthetic.main.loading_circular.view.ivBtn_close_circular
import kotlinx.android.synthetic.main.loading_circular.view.progressBar_circular
import kotlinx.android.synthetic.main.loading_circular.view.tv_error_circular
import kotlinx.android.synthetic.main.loading_circular.view.tv_loading_circular

class CircularViewType : ClassicViewTypeHandler() {
    private fun View.handleCancelable(result: LiveTask<*>) {
        if (result.isCancelable()) {
            ivBtn_close_circular.isVisible = true
            ivBtn_close_circular.setOnClickListener {
                result.cancel()
            }
        } else {
            ivBtn_close_circular.isVisible = false
        }
    }

    override val layoutId = R.layout.loading_circular

    override fun onLoading(
        view: View,
        inflatedView: View,
        parent: ViewGroup,
        liveTask: LiveTask<*>,
        result: Resource.Loading
    ) {
        inflatedView.apply {
            cl_error_circular.visibility = View.GONE
            progressBar_circular.visibility = View.VISIBLE
            tv_loading_circular.visibility = View.VISIBLE
            handleCancelable(liveTask)

        }
    }

    override fun onError(
        view: View,
        inflatedView: View,
        parent: ViewGroup,
        liveTask: LiveTask<*>,
        result: Resource.Error
    ) {
        inflatedView.apply {
            cl_error_circular.visibility = View.VISIBLE
            tv_error_circular.text = result.getErrorText(liveTask)
            handleCancelable(liveTask)
            if (liveTask.isRetryable()) {
                cl_container_circular.visibility = View.VISIBLE
                cl_error_circular.setOnClickListener {
                    liveTask.retry()
                }
            } else cl_container_circular.visibility = View.INVISIBLE
            progressBar_circular.visibility = View.GONE
            tv_loading_circular.visibility = View.GONE

        }
    }
}
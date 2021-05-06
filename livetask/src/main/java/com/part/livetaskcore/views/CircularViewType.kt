package com.part.livetaskcore.views

import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import androidx.core.view.isVisible
import com.part.livetask.R
import com.part.livetaskcore.Resource
import com.part.livetaskcore.livatask.LiveTask
import kotlinx.android.synthetic.main.loading_circular.view.*
import kotlin.coroutines.cancellation.CancellationException

class CircularViewType : ViewType() {
    private fun View.handleCancelable(result: LiveTask<*>) {
        if (result.isCancelable == true) {
            ivBtn_close_circular.isVisible = true
            ivBtn_close_circular.setOnClickListener {
                result.cancel()
            }
        } else {
            ivBtn_close_circular.isVisible = false
        }
    }

    override val layoutId = R.layout.loading_circular

    override fun loading(stateLayout: View?, parent: ViewGroup, result: LiveTask<*>, view: View) {
        stateLayout?.apply {
            cl_error_circular.visibility = View.GONE
            progressBar_circular.visibility = View.VISIBLE
            tv_loading_circular.visibility = View.VISIBLE
            handleCancelable(result)

        }
    }

    @OptIn(ExperimentalStdlibApi::class)
    override fun error(stateLayout: View?, parent: ViewGroup, result: LiveTask<*>, view: View) {
        stateLayout?.apply {
            if ((result.result() as Resource.Error).exception is CancellationException) {
                startAnimation(AnimationUtils.loadAnimation(context, R.anim.fade_out))
                view.tag = null
                parent.removeView(this)
            } else {
                cl_error_circular.visibility = View.VISIBLE
                tv_error_circular.text =
                    getErrorText(result, context.getString(R.string.task_error_text))
                handleCancelable(result)
                if (result.isRetryable == true) {
                    cl_container_circular.visibility = View.VISIBLE
                    cl_error_circular.setOnClickListener {
                        result.retry()
                    }
                } else cl_container_circular.visibility = View.INVISIBLE
                progressBar_circular.visibility = View.GONE
                tv_loading_circular.visibility = View.GONE
            }

        }
    }


}
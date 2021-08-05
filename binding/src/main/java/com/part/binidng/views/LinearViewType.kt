package com.part.binidng.views

import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import com.part.binidng.R
import com.part.livetaskcore.Resource
import com.part.livetaskcore.livatask.LiveTask
import kotlinx.android.synthetic.main.loading_linear.view.*
import kotlin.coroutines.cancellation.CancellationException


class LinearViewType : ClassicViewTypeHandler() {
    private fun View.handleCancelable(result: LiveTask<*>) {
        if (result.isCancelable() == true) {
            ivBtn_close_linear.visibility = View.VISIBLE
            ivBtn_close_linear.setOnClickListener {
                result.cancel()
            }
        } else ivBtn_close_linear.visibility = View.INVISIBLE
    }

    override val layoutId = R.layout.loading_linear

    override fun onLoading(
        view: View,
        inflatedView: View,
        parent: ViewGroup,
        liveTask: LiveTask<*>,
        result: Resource.Loading
    ) {

        inflatedView.apply {
            cl_error_linear.visibility = View.GONE
            progressBar_linear.visibility = View.VISIBLE
            tv_loading_linear.visibility = View.VISIBLE
            progressBar_linear_sandy_clock.visibility = View.VISIBLE
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
            if ((liveTask.result() as Resource.Error).exception is CancellationException) {
                startAnimation(AnimationUtils.loadAnimation(context, R.anim.fade_out))
                removeView(view, parent, inflatedView)
            } else {
                handleCancelable(liveTask)
                ivBtn_close_linear.visibility = View.VISIBLE
                cl_error_linear.visibility = View.VISIBLE
                ivBtn_close_linear.setOnClickListener {
                    startAnimation(
                        AnimationUtils.loadAnimation(
                            context,
                            R.anim.fade_out
                        )
                    )
                    inflatedView.tag = null
                    parent.removeView(this)
                }
                if (liveTask.isRetryable() == true) {
                    cl_container_linear.visibility = View.VISIBLE
                    cl_error_linear.setOnClickListener {
                        liveTask.retry()
                    }
                } else cl_container_linear.visibility = View.INVISIBLE
                progressBar_linear_sandy_clock.visibility = View.GONE
                progressBar_linear.visibility = View.GONE
                tv_loading_linear.visibility = View.GONE
            }

        }
    }
}
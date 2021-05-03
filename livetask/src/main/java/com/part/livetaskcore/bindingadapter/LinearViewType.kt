package com.part.livetaskcore.bindingadapter

import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import com.part.livetask.R
import com.part.livetaskcore.Resource
import com.part.livetaskcore.livatask.LiveTask
import kotlinx.android.synthetic.main.loading_linear.view.*
import kotlin.coroutines.cancellation.CancellationException

class LinearViewType : ViewType {
    private fun View.handleCancelable(result: LiveTask<*>) {
        if (result.isCancelable == true) {
            ivBtn_close_linear.visibility = View.VISIBLE
            ivBtn_close_linear.setOnClickListener {
                result.cancel()
            }
        } else ivBtn_close_linear.visibility = View.INVISIBLE
    }

    override val layoutId = R.layout.loading_linear

    override fun loading(stateLayout: View?, parent: ViewGroup, result: LiveTask<*>, view: View) {
        stateLayout?.let {
            it.apply {
                cl_error_linear.visibility = View.GONE
                progressBar_linear.visibility = View.VISIBLE
                tv_loading_linear.visibility = View.VISIBLE
                progressBar_linear_sandy_clock.visibility = View.VISIBLE
                handleCancelable(result)
            }

        }
    }

    @OptIn(ExperimentalStdlibApi::class)
    override fun error(stateLayout: View?, parent: ViewGroup, result: LiveTask<*>, view: View) {
        stateLayout?.let {
            it.apply {
                handleCancelable(result)
                if ((result.result() as Resource.Error).exception is CancellationException) {
                    startAnimation(AnimationUtils.loadAnimation(context, R.anim.fade_out))
                    view.tag = null
                    parent.removeView(it)
                } else {
                    ivBtn_close_linear.visibility = View.VISIBLE
                    cl_error_linear.visibility = View.VISIBLE
                    ivBtn_close_linear.setOnClickListener { _ ->
                        startAnimation(
                            AnimationUtils.loadAnimation(
                                context,
                                R.anim.fade_out
                            )
                        )
                        view.tag = null
                        parent.removeView(it)
                    }
                    if (result.isRetryable == true) {
                        cl_container_linear.visibility = View.VISIBLE
                        cl_error_linear.setOnClickListener {
                            result.retry()
                        }
                    } else cl_container_linear.visibility = View.INVISIBLE
                    progressBar_linear_sandy_clock.visibility = View.GONE
                    progressBar_linear.visibility = View.GONE
                    tv_loading_linear.visibility = View.GONE
                }
            }
        }
    }
}
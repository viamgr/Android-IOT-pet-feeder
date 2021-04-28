package com.part.livetaskcore.bindingadapter

import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import com.part.livetask.R
import com.part.livetaskcore.livatask.LiveTask
import com.viam.resource.Resource
import kotlinx.android.synthetic.main.loading_linear.view.*
import kotlin.coroutines.cancellation.CancellationException

/*

class IndicatorLoading : State {
    override fun loading(stateLayout: View?, parent: ViewGroup, result: LiveTask<*>, view: View) {
        stateLayout?.let {
            it.apply {
                cl_error_indicator.visibility = View.INVISIBLE
                progressBar_indicator.visibility = View.VISIBLE
                if (result.isCancelable) {
                    ivBtn_close_indicator.visibility = View.VISIBLE
                    ivBtn_close_indicator.setOnClickListener {
                        result.cancel()
                    }
                } else ivBtn_close_indicator.visibility = View.INVISIBLE
            }
        }
    }


    @OptIn(ExperimentalStdlibApi::class)
    override fun error(stateLayout: View?, parent: ViewGroup, result: LiveTask<*>, view: View) {
        stateLayout?.let {
            it.apply {
                if ((result.result() as Resource.Error).exception is CancellationException) {
                    startAnimation(AnimationUtils.loadAnimation(context, R.anim.fade_out))
                    view.tag = null
                    parent.removeView(it)
                } else {
                    */
/*val red = Color.parseColor("#E91E63")
                    val white = Color.parseColor("#ffffff")
                    val border = GradientDrawable()
                    border.setColor(red) //white background
                    border.setStroke(0, white) //black border with full opacity
                    it.background = border
                    tv_error_indicator.setTextColor(white)
                    ivBtn_close_indicator.setColorFilter(white)
                    iv_retry_indicator.setColorFilter(white)*//*


                    ivBtn_close_indicator.visibility = View.VISIBLE
                    cl_error_indicator.visibility = View.VISIBLE
                    ivBtn_close_indicator.setOnClickListener { _ ->
                        startAnimation(AnimationUtils.loadAnimation(context, R.anim.fade_out))
                        view.tag = null
                        parent.removeView(it)
                    }
                    if ((result as BaseLiveTask<*>).retryable) {
                        tv_error_indicator.visibility = View.VISIBLE
                        cl_error_indicator.setOnClickListener {
                            result.retry()
                        }
                    } else tv_error_indicator.visibility = View.INVISIBLE
                    progressBar_indicator.visibility = View.GONE
                }

            }
        }
    }
}

class SandyClockLoading : State {
    override fun loading(stateLayout: View?, parent: ViewGroup, result: LiveTask<*>, view: View) {
        stateLayout?.let {
            it.apply {
                cl_error_sandy_clock.visibility = View.GONE
                progressBar_sandy_clock.visibility = View.VISIBLE
                tv_loading_sandy_clock.visibility = View.VISIBLE
                if ((result as BaseLiveTask<*>).isCancelable) {
                    ivBtn_close_sandy_clock.visibility = View.VISIBLE
                    ivBtn_close_sandy_clock.setOnClickListener {
                        result.cancel()
                    }
                } else ivBtn_close_sandy_clock.visibility = View.INVISIBLE
            }
        }
    }

    @OptIn(ExperimentalStdlibApi::class)
    override fun error(stateLayout: View?, parent: ViewGroup, result: LiveTask<*>, view: View) {
        stateLayout?.let {
            it.apply {
                if ((result.result() as Resource.Error).exception is CancellationException) {
                    startAnimation(AnimationUtils.loadAnimation(context, R.anim.fade_out))
                    view.tag = null
                    parent.removeView(it)
                } else {
                    ivBtn_close_sandy_clock.visibility = View.VISIBLE
                    cl_error_sandy_clock.visibility = View.VISIBLE
                    ivBtn_close_sandy_clock.setOnClickListener { _ ->
                        startAnimation(AnimationUtils.loadAnimation(context, R.anim.fade_out))
                        view.tag = null
                        parent.removeView(it)
                    }
                    if ((result as BaseLiveTask<*>).retryable) {
                        cl_container_sandy_clock.visibility = View.VISIBLE
                        cl_error_sandy_clock.setOnClickListener {
                            result.retry()
                        }
                    } else cl_container_sandy_clock.visibility = View.INVISIBLE
                    progressBar_sandy_clock.visibility = View.GONE
                    tv_loading_sandy_clock.visibility = View.GONE
                }

            }
        }
    }
}

*/
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
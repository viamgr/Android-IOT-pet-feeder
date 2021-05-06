package com.part.livetaskcore.views

import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import androidx.core.view.isVisible
import com.part.livetask.R
import com.part.livetaskcore.Resource
import com.part.livetaskcore.livatask.LiveTask
import eightbitlab.com.blurview.BlurView
import eightbitlab.com.blurview.RenderScriptBlur
import kotlinx.android.synthetic.main.loading_blur.view.*
import kotlin.coroutines.cancellation.CancellationException

class BlurViewType(val errorText: String? = null) : ViewType() {
    private fun View.handleCancelable(result: LiveTask<*>) {
        close.isVisible = result.isCancelable == true
        if (result.isCancelable == true) {
            close.setOnClickListener {
                result.cancel()
            }
        }
    }

    override val layoutId = R.layout.loading_blur

    override fun loading(stateLayout: View, parent: ViewGroup, result: LiveTask<*>, view: View) {
        stateLayout.apply {
            val blurView = stateLayout.findViewById<BlurView>(R.id.blurView)
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN_MR1) {
                blurView.setupWith(stateLayout as ViewGroup)
                    .setBlurAlgorithm(RenderScriptBlur(stateLayout.context))
                    .setBlurRadius(2F)
                    .setBlurAutoUpdate(true)
                    .setHasFixedTransformationMatrix(true)
            }

            error.text = context.getString(R.string.loading)
            progress.isVisible = true
            retry.isVisible = false
            handleCancelable(result)

        }
    }

    @OptIn(ExperimentalStdlibApi::class)
    override fun error(stateLayout: View, parent: ViewGroup, result: LiveTask<*>, view: View) {
        stateLayout.apply {
            if ((result.result() as Resource.Error).exception is CancellationException) {
                startAnimation(AnimationUtils.loadAnimation(context, R.anim.fade_out))
                view.tag = null
                parent.removeView(this)
            } else {
                handleCancelable(result)
                retry.isVisible = result.isRetryable == true
                progress.isVisible = false
                progress.isVisible = false

                error.text =
                    getErrorText(result, errorText ?: context.getString(R.string.task_error_text))
                if (result.isRetryable == true) {
                    retry.setOnClickListener {
                        result.retry()
                    }
                }
            }
        }
    }
}
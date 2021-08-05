package com.part.binidng.views

import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import com.part.binidng.R
import com.part.livetaskcore.Resource
import com.part.livetaskcore.livatask.LiveTask
import com.part.livetaskcore.views.getErrorText
import eightbitlab.com.blurview.BlurView
import eightbitlab.com.blurview.RenderScriptBlur
import kotlinx.android.synthetic.main.loading_blur.view.*

class BlurViewType(val errorText: String? = null) : ClassicViewTypeHandler() {
    private fun View.handleCancelable(result: LiveTask<*>) {
        close.isVisible = result.isCancelable()
        if (result.isCancelable()) {
            close.setOnClickListener {
                result.cancel()
            }
        }
    }

    override val layoutId = R.layout.loading_blur

    override fun onLoading(
        view: View,
        inflatedView: View,
        parent: ViewGroup,
        liveTask: LiveTask<*>,
        result: Resource.Loading
    ) {
        inflatedView.apply {
            val blurView = inflatedView.findViewById<BlurView>(R.id.blurView)
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN_MR1) {
                blurView.setupWith(inflatedView as ViewGroup)
                    .setBlurAlgorithm(RenderScriptBlur(inflatedView.context))
                    .setBlurRadius(2F)
                    .setBlurAutoUpdate(true)
                    .setHasFixedTransformationMatrix(true)
            }

            error.text = context.getString(R.string.loading)
            progress.isVisible = true
            retry.isVisible = false
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
            handleCancelable(liveTask)
            retry.isVisible = liveTask.isRetryable() == true
            progress.isVisible = false
            progress.isVisible = false

            error.text =
                result.getErrorText(context.getString(R.string.task_error_text))
            if (liveTask.isRetryable() == true) {
                retry.setOnClickListener {
                    liveTask.retry()
                }
            }
        }
    }
}
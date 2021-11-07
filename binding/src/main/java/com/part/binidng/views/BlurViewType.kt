package com.part.binidng.views

import android.os.Build.VERSION
import android.os.Build.VERSION_CODES
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import com.part.binidng.R
import com.part.binidng.R.id
import com.part.livetaskcore.Resource
import com.part.livetaskcore.livatask.LiveTask
import com.part.livetaskcore.livatask.LoadingMessage
import com.part.livetaskcore.livatask.LoadingMessageBlock
import com.part.livetaskcore.views.getErrorText
import eightbitlab.com.blurview.BlurView
import eightbitlab.com.blurview.RenderScriptBlur
import kotlinx.android.synthetic.main.loading_blur.view.*

class BlurViewType : ClassicViewTypeHandler() {
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
        result: Resource.Loading,
        loadingMessageBlock: LoadingMessageBlock?
    ) {
        inflatedView.apply {
            addBlur(inflatedView)
            error.text = if (loadingMessageBlock == null) {
                context.getString(R.string.loading)
            } else {
                when (val loadingMessage = loadingMessageBlock(result.data)) {
                    is LoadingMessage.Text -> {
                        loadingMessage.message
                    }
                    is LoadingMessage.Res -> {
                        context.getString(loadingMessage.resId, loadingMessage.plurals)
                    }
                }
            }

            progress.isVisible = true
            retry.isVisible = false
            handleCancelable(liveTask)

        }
    }

    private fun addBlur(inflatedView: View) {
        val blurView = inflatedView.findViewById<BlurView>(id.blurView)
        if (VERSION.SDK_INT >= VERSION_CODES.JELLY_BEAN_MR1) {
            blurView.setupWith(inflatedView as ViewGroup)
                .setBlurAlgorithm(RenderScriptBlur(inflatedView.context))
                .setBlurRadius(2F)
                .setBlurAutoUpdate(true)
                .setHasFixedTransformationMatrix(true)
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
            addBlur(inflatedView)

            handleCancelable(liveTask)
            retry.isVisible = liveTask.isRetryable() == true
            progress.isVisible = false

            error.text = result.getErrorText(liveTask)
            if (liveTask.isRetryable()) {
                retry.setOnClickListener {
                    liveTask.retry()
                }
            }
        }
    }
}
package com.part.binidng.views

import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import com.part.binidng.bindingadapter.getViewParent
import com.part.livetask.R
import com.part.livetaskcore.Resource
import com.part.livetaskcore.livatask.LiveTask
import com.part.livetaskcore.views.ViewTypeHandler
import kotlin.coroutines.cancellation.CancellationException

abstract class ClassicViewTypeHandler : ViewTypeHandler {
    abstract val layoutId: Int
    fun onUpdate(liveTask: LiveTask<*>?, result: Resource<*>?, view: View) {
        liveTask?.let {
            result?.let {
                val viewParent = getViewParent(view, this)
                when (it) {
                    is Resource.Success -> {
                        success(
                            view,
                            viewParent.view,
                            viewParent.parent as ViewGroup,
                            liveTask,
                            it
                        )
                    }
                    is Resource.Loading -> {
                        loading(view, viewParent.view, viewParent.parent as ViewGroup, liveTask, it)

                    }
                    is Resource.Error -> {
                        error(view, viewParent.view, viewParent.parent as ViewGroup, liveTask, it)
                    }
                }
            }
        }
    }

    fun loading(
        view: View,
        inflatedView: View,
        parent: ViewGroup,
        liveTask: LiveTask<*>,
        result: Resource.Loading,
    ) {
        onLoading(view, inflatedView, parent, liveTask, result)
    }

    protected open fun onLoading(
        view: View,
        inflatedView: View,
        parent: ViewGroup,
        liveTask: LiveTask<*>,
        result: Resource.Loading,
    ) {

    }

    fun success(
        view: View,
        inflatedView: View,
        parent: ViewGroup,
        liveTask: LiveTask<*>,
        result: Resource.Success<*>,
    ) {
        onSuccess(view, inflatedView, parent, liveTask, result)
    }

    protected open fun onSuccess(
        view: View,
        inflatedView: View,
        parent: ViewGroup,
        liveTask: LiveTask<*>,
        result: Resource.Success<*>,
    ) {
        removeView(view, parent, inflatedView)
    }

    fun removeView(
        view: View,
        parent: ViewGroup,
        inflatedView: View
    ) {
        view.tag = null
        parent.removeView(inflatedView)
    }

    fun error(
        view: View,
        inflatedView: View,
        parent: ViewGroup,
        liveTask: LiveTask<*>,
        result: Resource.Error
    ) {
        if (result.exception is CancellationException) {
//            hideWithAnimate(view)
            removeView(view, parent, inflatedView)
        } else {
            onError(view, inflatedView, parent, liveTask, result)
        }
    }

    protected open fun onError(
        view: View,
        inflatedView: View,
        parent: ViewGroup,
        liveTask: LiveTask<*>,
        result: Resource.Error
    ) {

    }

    open fun hideWithAnimate(view: View) {
        view.startAnimation(AnimationUtils.loadAnimation(view.context, R.anim.fade_out))
    }
}

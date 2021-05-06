package com.part.livetaskcore.views

import android.view.View
import android.view.ViewGroup
import com.part.livetaskcore.Resource
import com.part.livetaskcore.livatask.LiveTask
import com.part.livetaskcore.livatask.ViewException

/**
 * Interface that allows controlling UI on each state. You can implement [ViewType] to
 * handle your own User Interface states if you want to use our DataBinding Adapter.
 * */
abstract class ViewType {
    abstract val layoutId: Int

    abstract fun loading(
        stateLayout: View,
        parent: ViewGroup,
        result: LiveTask<*>,
        view: View,
    )

    abstract fun error(
        stateLayout: View,
        parent: ViewGroup,
        result: LiveTask<*>,
        view: View,
    )

    open fun success(
        stateLayout: View,
        parent: ViewGroup,
        result: LiveTask<*>,
        view: View,
    ) {
        view.tag = null
        parent.removeView(stateLayout)
    }

    fun getErrorText(result: LiveTask<*>, errorText: String) =
        if ((result.result() as Resource.Error).exception is ViewException) {
            ((result.result() as Resource.Error).exception as ViewException).viewMessage
        } else {
            errorText
        }
}


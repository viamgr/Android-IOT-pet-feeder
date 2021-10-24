package com.part.livetaskcore.views

import android.view.View
import android.view.ViewGroup
import com.part.livetaskcore.livatask.LiveTask

/**
 * Interface that allows controlling UI on each state. You can implement [BaseViewType] to
 * handle your own User Interface states if you want to use our DataBinding Adapter.
 * */
abstract class BaseViewType {

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
}


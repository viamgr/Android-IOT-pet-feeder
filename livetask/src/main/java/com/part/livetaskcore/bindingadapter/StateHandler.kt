package com.part.livetaskcore.bindingadapter

import android.view.View
import android.view.ViewGroup
import com.part.livetaskcore.livatask.LiveTask

/**
 * Interface that allows controlling UI on each state. You can implement [ViewType] to
 * handle your own User Interface states if you want to use our DataBinding Adapter.
 * */
interface ViewType {
    val layoutId: Int

    fun loading(
        stateLayout: View?,
        parent: ViewGroup,
        result: LiveTask<*>,
        view: View
    )

    fun error(
        stateLayout: View?,
        parent: ViewGroup,
        result: LiveTask<*>,
        view: View
    )

    fun success(
        stateLayout: View?,
        parent: ViewGroup,
        result: LiveTask<*>,
        view: View
    ) {
        stateLayout?.let {
            view.tag = null
            parent.removeView(it)
        }
    }
}


package com.part.binidng.bindingadapter

import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.view.ViewCompat
import com.part.binidng.views.ClassicViewTypeHandler

data class ViewParent(
    val view: View,
    val parent: Any,
)

abstract class ViewWrapper {
    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    fun inflateLayout(view: ViewGroup, layoutId: Int): View {
        val layout = LayoutInflater.from(view.context)
            .inflate(layoutId, view, false)
        layout.id = View.generateViewId()
        return layout
    }

    abstract fun place(): ViewParent
}

class ConstraintLayoutViewWrapper(
    private val parent: ConstraintLayout,
    private val view: View,
    val layout: Int,
) :
    ViewWrapper() {
    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    override fun place(): ViewParent {
        if (view.tag == null || parent.getViewById(view.tag as Int) == null) {
            val layout = inflateLayout(parent, layout)
            ViewCompat.setElevation(layout, Float.MAX_VALUE)
            setConstraint(layout)
            layout.bringToFront()
            view.tag = layout.id
        }
        return ViewParent(parent.getViewById(view.tag as Int), parent)
    }

    private fun setConstraint(
        layout: View,
    ) {
        val constraintSet = ConstraintSet()
        constraintSet.clone(parent)
        constraintSet.connect(
            layout.id,
            ConstraintSet.TOP,
            view.id,
            ConstraintSet.TOP
        )
        constraintSet.connect(
            layout.id,
            ConstraintSet.BOTTOM,
            view.id,
            ConstraintSet.BOTTOM
        )
        constraintSet.connect(
            layout.id,
            ConstraintSet.END,
            view.id,
            ConstraintSet.END
        )
        constraintSet.connect(
            layout.id,
            ConstraintSet.START,
            view.id,
            ConstraintSet.START
        )
        parent.addView(layout)
        constraintSet.applyTo(parent)
    }
}

class ViewGroupViewWrapper(val view: View, private val layout: Int, private val parent: ViewGroup) :
    ViewWrapper() {
    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    override fun place(): ViewParent {
        if (view.tag == null) {
            val layout = inflateLayout(parent, layout)
            parent.addView(layout)
            layout.translationZ = 10F
            layout.bringToFront()
            parent.bringChildToFront(layout)

            view.tag = layout.id
        }
        return ViewParent(parent.findViewById(view.tag as Int), parent)
    }
}

private fun typeHandler(view: View, layout: Int): ViewWrapper = when (view) {
    is ConstraintLayout ->
        ConstraintLayoutViewWrapper(view, view, layout)

    else -> {
        if (view.parent is ConstraintLayout)
            ConstraintLayoutViewWrapper(view.parent as ConstraintLayout, view, layout)
        else {
            val parent = if (view is ViewGroup)
                view
            else {
                view.parent as ViewGroup
            }
            ViewGroupViewWrapper(view, layout, parent)
        }
    }
}

fun getViewParent(
    view: View,
    loadingViewType: ClassicViewTypeHandler,
): ViewParent {
    if (view.id == -1) view.id = ViewCompat.generateViewId()
    val placement = typeHandler(view, loadingViewType.layoutId).place()
    return ViewParent(placement.view, placement.parent)
}

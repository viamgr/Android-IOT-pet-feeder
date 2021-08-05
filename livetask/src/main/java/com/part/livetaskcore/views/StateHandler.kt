package com.part.livetaskcore.views

/**
 * Interface that allows controlling UI on each state. You can implement [ViewType] to
 * handle your own User Interface states if you want to use our DataBinding Adapter.
 * */
interface ViewType
enum class DefaultViewTypes : ViewType {
    Circular, Linear, Blur
}
package com.part.livetaskcore.livatask

import androidx.annotation.StringRes

sealed class LoadingMessage {
    class Res(@StringRes val resId: Int, vararg val plurals: Any? = emptyArray()) : LoadingMessage()
    class Text(val message: String) : LoadingMessage()
}



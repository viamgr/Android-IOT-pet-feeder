package com.part.livetaskcore.views

import com.part.livetaskcore.Resource
import com.part.livetaskcore.livatask.ViewException

fun Resource.Error.getErrorText(default: String) =
    if (this.exception is ViewException) {
        this.exception.viewMessage
    } else {
        default
    }
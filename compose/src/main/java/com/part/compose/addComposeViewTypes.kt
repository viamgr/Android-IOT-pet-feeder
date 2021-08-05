package com.part.compose

import com.part.livetaskcore.LiveTaskManager
import com.part.livetaskcore.views.DefaultViewTypes

fun LiveTaskManager.Builder.addComposeViewTypes(): LiveTaskManager.Builder {
    setViewType(DefaultViewTypes.Linear, ComposeLinearViewType())
    setViewType(DefaultViewTypes.Blur, ComposeLinearViewType())
    setViewType(DefaultViewTypes.Circular, ComposeLinearViewType())
    return this
}
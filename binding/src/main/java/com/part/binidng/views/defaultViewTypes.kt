package com.part.binidng.views

import com.part.livetaskcore.LiveTaskManager
import com.part.livetaskcore.views.DefaultViewTypes

fun LiveTaskManager.Builder.addDefaultClassicViewTypes(): LiveTaskManager.Builder {
    setViewType(DefaultViewTypes.Circular, CircularViewType())
    setViewType(DefaultViewTypes.Linear, LinearViewType())
    setViewType(DefaultViewTypes.Blur, BlurViewType())
    return this
}
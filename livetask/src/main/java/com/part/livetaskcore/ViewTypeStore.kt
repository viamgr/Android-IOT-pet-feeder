package com.part.livetaskcore

import com.part.livetaskcore.views.ViewType
import com.part.livetaskcore.views.ViewTypeHandler

data class ViewTypeStore(
    val viewType: ViewType,
    val viewTypeHandler: ViewTypeHandler
)
package com.part.compose

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import com.part.livetaskcore.LiveTaskManager
import com.part.livetaskcore.livatask.LiveTask


@Composable
fun <T> ReactToTask(
    liveTask: LiveTask<T>,
    modifier: Modifier = Modifier,
    liveTaskManager: LiveTaskManager = LiveTaskManager.instance,
    children: @Composable () -> Unit,
) {
    val result by liveTask.liveResult.observeAsState()

    val viewTypeHandler =
        liveTaskManager.findViewTypeHandler<ComposeViewTypeHandler>(liveTask.loadingViewType())
    viewTypeHandler.OnUpdate(
        liveTask,
        result,
        modifier, children
    )
}

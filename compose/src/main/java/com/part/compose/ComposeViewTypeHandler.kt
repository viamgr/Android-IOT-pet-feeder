package com.part.compose

import androidx.annotation.CallSuper
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.part.livetaskcore.Resource
import com.part.livetaskcore.livatask.LiveTask
import com.part.livetaskcore.views.ViewTypeHandler

abstract class ComposeViewTypeHandler : ViewTypeHandler {
    @Composable
    fun OnUpdate(
        liveTask: LiveTask<*>,
        result: Resource<*>?,
        modifier: Modifier,
        children: @Composable () -> Unit,
    ) {
        when (result) {
            is Resource.Success -> {
                Success(liveTask, result, modifier, children)
            }
            is Resource.Loading -> {
                Loading(liveTask, result, modifier, children)

            }
            is Resource.Error -> {
                if (result.exception is kotlinx.coroutines.CancellationException) {
                    Cancel(liveTask, result, modifier, children)

                } else {
                    Error(liveTask, result, modifier, children)
                }

            }
            else -> {
                children()
            }
        }
    }

    @Composable
    abstract fun Loading(
        liveTask: LiveTask<*>,
        result: Resource.Loading,
        modifier: Modifier,
        children: @Composable () -> Unit,
    )

    @Composable
    @CallSuper
    open fun Success(
        liveTask: LiveTask<*>,
        result: Resource.Success<*>,
        modifier: Modifier,
        children: @Composable () -> Unit,
    ) {
        children()
    }

    @Composable
    abstract fun Error(
        liveTask: LiveTask<*>,
        result: Resource.Error,
        modifier: Modifier,
        children: @Composable () -> Unit,
    )

    @Composable
    @CallSuper
    open fun Cancel(
        liveTask: LiveTask<*>,
        result: Resource.Error,
        modifier: Modifier,
        children: @Composable () -> Unit,
    ) {
        children()
    }
}

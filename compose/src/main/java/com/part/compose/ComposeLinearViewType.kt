package com.part.compose

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition
import com.part.livetaskcore.Resource
import com.part.livetaskcore.livatask.LiveTask
import com.part.livetaskcore.views.getErrorText


class ComposeLinearViewType : ComposeViewTypeHandler() {

    @Composable
    override fun Loading(
        liveTask: LiveTask<*>,
        result: Resource.Loading,
        modifier: Modifier,
        children: @Composable () -> Unit,
    ) {
        Box(modifier = modifier.fillMaxWidth()) {
            AddChildrenInBox(modifier, children)

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.End,
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .alpha(1f)
                    .disableClickOnSiblings()
            ) {
                Text(
                    stringResource(R.string.loading),
                    Modifier
                        .fillMaxWidth()
                        .weight(1F)
                )

                AddLoadingAnimation()

                HandleCancelable(liveTask)
            }
        }


    }

    @Composable
    private fun Modifier.disableClickOnSiblings() =
        clickable(onClick = {}, indication = null,
            interactionSource = remember { MutableInteractionSource() })

    @Composable
    private fun AddChildrenInBox(
        modifier: Modifier,
        children: @Composable () -> Unit
    ) {
        Box(
            modifier = modifier
                .wrapContentSize()
                .alpha(0f)
        ) {
            children()
        }
    }

    @Composable
    private fun AddLoadingAnimation() {
        val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.horizontal_loading))
        val progress by animateLottieCompositionAsState(
            composition,
            iterations = LottieConstants.IterateForever
        )
        LottieAnimation(
            composition = composition,
            progress = progress,
            modifier = Modifier
                .width(32.dp)
                .height(32.dp)
        )
    }

    @Composable
    private fun HandleCancelable(liveTask: LiveTask<*>) {
        if (liveTask.isCancelable()) {
            Image(
                painterResource(R.drawable.ic_baseline_close_24),
                colorFilter = ColorFilter.tint(Color.Black),
                contentDescription = "",
                modifier = Modifier
                    .padding(end = 16.dp)
                    .clickable {
                        liveTask.cancel()
                    }
            )

        }
    }

    @Composable
    override fun Error(
        liveTask: LiveTask<*>,
        result: Resource.Error,
        modifier: Modifier,
        children: @Composable () -> Unit,
    ) {
        Box(modifier = modifier.fillMaxWidth()) {
            children()
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.End,
                modifier = Modifier
                    .background(Color.White)
                    .matchParentSize()
                    .wrapContentHeight()
                    .disableClickOnSiblings()
            ) {

                Text(
                    result.getErrorText(stringResource(R.string.task_error_text)),
                    modifier = Modifier

                        .weight(1F)
                        .fillMaxWidth(),
                    overflow = TextOverflow.Ellipsis,

                    )

                HandleRetryable(liveTask)
                HandleCancelable(liveTask)
            }

        }
    }

    @Composable
    private fun HandleRetryable(liveTask: LiveTask<*>) {
        if (liveTask.isRetryable()) {
            Image(
                painterResource(R.drawable.ic_baseline_refresh_24),
                colorFilter = ColorFilter.tint(Color.Black),
                contentDescription = "",
                modifier = Modifier
                    .padding(end = 16.dp)
                    .clickable {
                        liveTask.retry()
                    }
            )

        }
    }

}
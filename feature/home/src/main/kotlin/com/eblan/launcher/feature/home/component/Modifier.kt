/*
 *
 *   Copyright 2023 Einstein Blanco
 *
 *   Licensed under the GNU General Public License v3.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       https://www.gnu.org/licenses/gpl-3.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 *
 */
package com.eblan.launcher.feature.home.component

import android.graphics.Paint
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.PagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.layout
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.eblan.launcher.domain.model.userdata.EblanAction
import com.eblan.launcher.domain.model.userdata.EblanActionType
import com.eblan.launcher.domain.model.userdata.PagerTransitionEffect
import com.eblan.launcher.feature.home.model.SharedElementKey
import com.eblan.launcher.feature.home.util.DRAG_HANDLE_SIZE
import com.eblan.launcher.feature.home.util.handleEblanAction
import com.eblan.launcher.ui.local.LocalLauncherApps
import com.eblan.launcher.ui.local.LocalStatusBarManager
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.roundToInt

@Composable
internal fun Modifier.swipeGestures(
    // State
    enabled: Boolean = true,
    // Gestures
    swipeDown: EblanAction,
    swipeUp: EblanAction,
    // Actions
    onOpenAppDrawer: () -> Unit
): Modifier {
    val context = LocalContext.current
    val density = LocalDensity.current
    val scope = rememberCoroutineScope()
    val launcherApps = LocalLauncherApps.current
    val androidStatusBarManagerWrapper = LocalStatusBarManager.current

    val currentOnOpenAppDrawer by rememberUpdatedState(onOpenAppDrawer)

    return if ((
            swipeUp.eblanActionType != EblanActionType.None ||
                swipeDown.eblanActionType != EblanActionType.None
            ) && enabled
    ) {
        val swipeY = remember { Animatable(0f) }

        val maxSwipeY = with(density) {
            40.dp.roundToPx()
        }

        pointerInput(key1 = Unit) {
            detectVerticalDragGestures(
                onDragStart = {
                    scope.launch {
                        swipeY.snapTo(0f)
                    }
                },
                onVerticalDrag = { _, dragAmount ->
                    scope.launch {
                        swipeY.snapTo(swipeY.value + dragAmount)
                    }
                },
                onDragCancel = {
                    scope.launch {
                        swipeY.animateTo(0f)
                    }
                },
                onDragEnd = {
                    scope.launch {
                        val lastSwipeY = swipeY.value

                        swipeY.animateTo(0f)

                        when {
                            lastSwipeY <= -maxSwipeY -> {
                                handleEblanAction(
                                    // Context and Action
                                    context = context,
                                    eblanAction = swipeUp,
                                    // System Wrappers
                                    launcherApps = launcherApps,
                                    statusBarManagerWrapper = androidStatusBarManagerWrapper,
                                    // Callbacks
                                    onOpenAppDrawer = currentOnOpenAppDrawer
                                )
                            }

                            lastSwipeY >= maxSwipeY -> {
                                handleEblanAction(
                                    // Context and Action
                                    context = context,
                                    eblanAction = swipeDown,
                                    // System Wrappers
                                    launcherApps = launcherApps,
                                    statusBarManagerWrapper = androidStatusBarManagerWrapper,
                                    // Callbacks
                                    onOpenAppDrawer = currentOnOpenAppDrawer
                                )
                            }
                        }
                    }
                },
            )
        }.offset {
            IntOffset(
                x = 0,
                y = swipeY.value.roundToInt().coerceIn(-maxSwipeY..maxSwipeY),
            )
        }
    } else {
        this
    }
}

internal fun Modifier.popup(
    // Dimensions
    width: Int,
    height: Int,
    // Position
    x: Int,
    y: Int
): Modifier = layout { measurable, constraints ->
    val placeable = measurable.measure(
        constraints.copy(minWidth = 0, minHeight = 0),
    )

    val parentCenterX = x + width / 2

    val topY = y - placeable.height
    val bottomY = y + height

    val childX = parentCenterX - placeable.width / 2
    val childY = if (topY < 0) bottomY else topY

    layout(constraints.maxWidth, constraints.maxHeight) {
        placeable.place(
            x = childX.coerceIn(0, constraints.maxWidth - placeable.width),
            y = childY.coerceIn(0, constraints.maxHeight - placeable.height),
        )
    }
}

@Composable
internal fun Modifier.gridItemSharedElement(
    // Transition State
    sharedTransitionScope: SharedTransitionScope,
    sharedElementKey: SharedElementKey,
    // Visibility Control
    enabled: Boolean,
    visible: Boolean
): Modifier = if (enabled && visible) {
    with(sharedTransitionScope) {
        sharedElementWithCallerManagedVisibility(
            sharedContentState = rememberSharedContentState(key = sharedElementKey),
            visible = true,
        )
    }
} else {
    this
}

internal fun Modifier.whiteBox(
    textColor: Color,
    visible: Boolean,
): Modifier = if (visible) {
    drawWithCache {
        val strokeWidth = 3.dp.toPx()

        val cornerRadius = 5.dp.toPx()

        val inset = strokeWidth / 2f

        val paint = Paint().apply {
            isAntiAlias = true
            style = Paint.Style.STROKE
            this.strokeWidth = strokeWidth
            color = textColor.copy(alpha = 0.3f).toArgb()
            setShadowLayer(12.dp.toPx(), 0f, 0f, textColor.toArgb())
        }

        onDrawBehind {
            drawContext.canvas.nativeCanvas.drawRoundRect(
                inset,
                inset,
                size.width - inset,
                size.height - inset,
                cornerRadius,
                cornerRadius,
                paint,
            )
        }
    }
} else {
    this
}

internal fun Modifier.pagerTransitionEffect(
    pagerState: PagerState,
    page: Int,
    effect: PagerTransitionEffect,
): Modifier = graphicsLayer {
    val pageOffset = (pagerState.currentPage - page) + pagerState.currentPageOffsetFraction
    val absoluteOffset = abs(pageOffset).coerceIn(0f, 1f)
    val pageWidth = size.width

    when (effect) {
        PagerTransitionEffect.None -> Unit

        PagerTransitionEffect.Fade -> {
            alpha = 1f - absoluteOffset
        }

        PagerTransitionEffect.ZoomOut -> {
            val scale = 1f - (0.25f * absoluteOffset)
            scaleX = scale
            scaleY = scale
            alpha = 1f - (0.5f * absoluteOffset)
        }

        PagerTransitionEffect.Depth -> {
            val scale = 1f - (0.2f * absoluteOffset)
            scaleX = scale
            scaleY = scale
            alpha = 1f - (0.5f * absoluteOffset)
            translationX = -pageOffset * pageWidth * 0.25f
        }

        PagerTransitionEffect.Cube -> {
            rotationY = -90f * pageOffset
            transformOrigin = TransformOrigin(
                pivotFractionX = if (pageOffset > 0f) 0f else 1f,
                pivotFractionY = 0.5f,
            )
            cameraDistance = maxOf(size.width * 1.1f, 12f * density)
        }

        PagerTransitionEffect.Flip -> {
            rotationY = -180f * pageOffset
            alpha = 1f - (0.2f * absoluteOffset)
            cameraDistance = maxOf(size.width * 1.1f, 12f * density)
        }

        PagerTransitionEffect.Parallax -> {
            translationX = -pageOffset * pageWidth * 0.35f
        }

        PagerTransitionEffect.Stack -> {
            val scale = 1f - (0.12f * absoluteOffset)
            scaleX = scale
            scaleY = scale
            translationX = -pageOffset * pageWidth * 0.2f
            alpha = 1f - (0.25f * absoluteOffset)
        }
    }
}

@Composable
internal fun Modifier.resizeHandleModifier(color: Color): Modifier = size(DRAG_HANDLE_SIZE)
    .drawBehind {
        drawCircle(
            color = color,
            radius = 5.dp.toPx(),
        )
    }

@Composable
internal fun Modifier.dashedResizeOutline(color: Color): Modifier {
    val density = LocalDensity.current

    val dashLength = with(density) { 8.dp.toPx() }
    val gapLength = with(density) { 5.dp.toPx() }

    val dashPhase = rememberInfiniteTransition(label = "resizeOutline")
        .animateFloat(
            initialValue = 0f,
            targetValue = dashLength + gapLength,
            animationSpec = infiniteRepeatable(
                animation = tween(
                    durationMillis = 5000,
                    easing = LinearEasing,
                ),
            ),
            label = "resizeOutlinePhase",
        )

    return drawWithCache {
        val stroke = Stroke(
            width = 2.dp.toPx(),
            pathEffect = PathEffect.dashPathEffect(
                intervals = floatArrayOf(dashLength, gapLength),
                phase = dashPhase.value,
            ),
        )

        onDrawBehind {
            drawRoundRect(
                color = color,
                cornerRadius = CornerRadius(12.dp.toPx()),
                style = stroke,
            )
        }
    }
}

@Composable
internal fun Modifier.gridItemScaleAnimation(
    animations: Boolean,
    hasInteraction: Boolean,
): Modifier {
    val scale = remember { Animatable(1f) }

    LaunchedEffect(key1 = hasInteraction) {
        if (hasInteraction) {
            scale.snapTo(OVERLAY_SCALE)
        } else if (animations) {
            scale.animateTo(1f)
        } else {
            scale.snapTo(1f)
        }
    }

    return scale(scale.value)
}

internal const val OVERLAY_SCALE = 1.1f

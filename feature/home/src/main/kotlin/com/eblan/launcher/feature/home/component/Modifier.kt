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

import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.offset
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.layout
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.eblan.launcher.domain.model.userdata.EblanAction
import com.eblan.launcher.domain.model.userdata.EblanActionType
import com.eblan.launcher.feature.home.model.SharedElementKey
import com.eblan.launcher.feature.home.util.handleEblanAction
import com.eblan.launcher.ui.local.LocalLauncherApps
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@Composable
internal fun Modifier.swipeGestures(
    swipeDown: EblanAction,
    swipeUp: EblanAction,
    enabled: Boolean = true,
    onOpenAppDrawer: () -> Unit,
): Modifier {
    val context = LocalContext.current

    val density = LocalDensity.current

    val scope = rememberCoroutineScope()

    val launcherApps = LocalLauncherApps.current

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
                                    context = context,
                                    eblanAction = swipeUp,
                                    launcherApps = launcherApps,
                                    onOpenAppDrawer = currentOnOpenAppDrawer,
                                )
                            }

                            lastSwipeY >= maxSwipeY -> {
                                handleEblanAction(
                                    context = context,
                                    eblanAction = swipeDown,
                                    launcherApps = launcherApps,
                                    onOpenAppDrawer = currentOnOpenAppDrawer,
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
    width: Int,
    height: Int,
    x: Int,
    y: Int,
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
    enabled: Boolean = true,
    sharedElementKey: SharedElementKey,
    sharedTransitionScope: SharedTransitionScope,
    visible: Boolean,
): Modifier = this
    .run {
        if (enabled && visible) {
            with(sharedTransitionScope) {
                sharedElementWithCallerManagedVisibility(
                    sharedContentState = rememberSharedContentState(key = sharedElementKey),
                    visible = true,
                )
            }
        } else {
            this
        }
    }

@Composable
internal fun Modifier.gridItemScaleAnimation(
    isVisibleOverlay: Boolean,
    animations: Boolean,
    scale: Animatable<Float, AnimationVector1D>,
): Modifier {
    LaunchedEffect(
        key1 = isVisibleOverlay,
        key2 = animations,
    ) {
        if (isVisibleOverlay && animations) {
            scale.snapTo(1f)
        }
    }

    return if (animations) {
        graphicsLayer {
            scaleX = scale.value
            scaleY = scale.value
        }
    } else {
        this
    }
}

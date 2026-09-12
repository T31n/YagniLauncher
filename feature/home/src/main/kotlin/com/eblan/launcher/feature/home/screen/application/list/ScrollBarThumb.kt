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
package com.eblan.launcher.feature.home.screen.application.list

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@Composable
internal fun ScrollBarThumb(
    modifier: Modifier = Modifier,
    lazyListState: LazyListState,
    paddingValues: PaddingValues,
    onScrollToItem: suspend (Int) -> Unit,
) {
    val density = LocalDensity.current

    val scope = rememberCoroutineScope()

    val bottomPadding = with(density) {
        paddingValues.calculateBottomPadding().roundToPx()
    }

    val thumbHeight by remember(key1 = lazyListState) {
        derivedStateOf {
            with(density) {
                (lazyListState.layoutInfo.viewportSize.height / 4).toDp()
            }
        }
    }

    val viewPortThumbY by remember(key1 = lazyListState) {
        derivedStateOf {
            getViewPortThumbY(
                lazyListState = lazyListState,
                density = density,
                thumbHeight = thumbHeight,
                bottomPadding = bottomPadding,
            )
        }
    }

    var isDraggingThumb by remember { mutableStateOf(false) }

    var thumbY by remember { mutableFloatStateOf(0f) }

    val animatedThumbY by remember {
        derivedStateOf {
            if (isDraggingThumb) thumbY else viewPortThumbY
        }
    }

    Row(modifier = modifier) {
        Box(
            modifier = Modifier
                .width(10.dp)
                .fillMaxHeight()
                .padding(bottom = paddingValues.calculateBottomPadding())
                .background(
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f),
                    shape = RoundedCornerShape(10.dp),
                ).pointerInput(lazyListState) {
                    detectTapGestures(
                        onTap = {
                            handleOnTap(
                                lazyListState = lazyListState,
                                bottomPadding = bottomPadding,
                                density = density,
                                thumbHeight = thumbHeight,
                                offset = it,
                                scope = scope,
                                onScrollToItem = onScrollToItem,
                            )
                        },
                    )
                },
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(thumbHeight)
                    .offset {
                        IntOffset(
                            x = 0,
                            y = animatedThumbY.roundToInt(),
                        )
                    }
                    .background(
                        color = MaterialTheme.colorScheme.primary,
                        shape = RoundedCornerShape(10.dp),
                    ).pointerInput(lazyListState) {
                        detectDragGestures(
                            onDragStart = {
                                thumbY = viewPortThumbY
                                isDraggingThumb = true
                            },
                            onDrag = { _, dragAmount ->
                                handleVerticalDrag(
                                    lazyListState = lazyListState,
                                    density = density,
                                    thumbHeight = thumbHeight,
                                    bottomPadding = bottomPadding,
                                    thumbY = thumbY,
                                    deltaY = dragAmount.y,
                                    scope = scope,
                                    onScrollToItem = onScrollToItem,
                                    onUpdateThumbY = {
                                        thumbY = it
                                    },
                                )
                            },
                            onDragEnd = {
                                isDraggingThumb = false
                            },
                            onDragCancel = {
                                isDraggingThumb = false
                            },
                        )
                    },
            )
        }
    }
}

private fun handleOnTap(
    lazyListState: LazyListState,
    bottomPadding: Int,
    density: Density,
    thumbHeight: Dp,
    offset: Offset,
    scope: CoroutineScope,
    onScrollToItem: suspend (Int) -> Unit,
) {
    val viewportHeight = lazyListState.layoutInfo.viewportSize.height - bottomPadding

    val maxThumbY = (viewportHeight - with(density) { thumbHeight.roundToPx() }).coerceAtLeast(0)

    val targetThumbY = (offset.y - with(density) { thumbHeight.roundToPx() / 2f }).coerceIn(
        0f,
        maxThumbY.toFloat(),
    )

    val totalItems = lazyListState.layoutInfo.totalItemsCount

    val item = (targetThumbY / maxThumbY.coerceAtLeast(1)) * (totalItems - 1)

    scope.launch {
        onScrollToItem(
            item.roundToInt().coerceAtMost(totalItems - 1),
        )
    }
}

private fun handleVerticalDrag(
    lazyListState: LazyListState,
    density: Density,
    thumbHeight: Dp,
    bottomPadding: Int,
    thumbY: Float,
    deltaY: Float,
    scope: CoroutineScope,
    onScrollToItem: suspend (Int) -> Unit,
    onUpdateThumbY: (Float) -> Unit,
) {
    if (deltaY == 0f) return

    val layoutInfo = lazyListState.layoutInfo
    val visibleItems = layoutInfo.visibleItemsInfo

    val avgItemSize = visibleItems.sumOf { it.size } / visibleItems.size

    val totalItems = layoutInfo.totalItemsCount
    val viewportHeight = layoutInfo.viewportSize.height

    val thumbHeightPx = with(density) { thumbHeight.toPx() }

    val availableHeight = (viewportHeight - thumbHeightPx - bottomPadding).coerceAtLeast(0f)

    val newThumbY = (thumbY + deltaY).coerceIn(0f, availableHeight)

    val progress = if (availableHeight > 0f) {
        (newThumbY / availableHeight).coerceIn(0f, 1f)
    } else {
        0f
    }

    val totalContentHeight = totalItems * avgItemSize
    val scrollableHeight = (totalContentHeight - viewportHeight).coerceAtLeast(0)

    val targetScrollY = (progress * scrollableHeight).coerceIn(0f, scrollableHeight.toFloat())

    val targetIndex = (targetScrollY / avgItemSize).toInt().coerceIn(0, totalItems - 1)

    onUpdateThumbY(newThumbY)

    scope.launch {
        onScrollToItem(targetIndex)
    }
}

private fun getViewPortThumbY(
    lazyListState: LazyListState,
    density: Density,
    thumbHeight: Dp,
    bottomPadding: Int,
): Float {
    val layoutInfo = lazyListState.layoutInfo
    val visibleItems = layoutInfo.visibleItemsInfo

    val firstItem = visibleItems.first()

    val visibleHeight = visibleItems.sumOf { it.size }
    val avgItemSize = visibleHeight / visibleItems.size

    val totalItems = layoutInfo.totalItemsCount
    val totalContentHeight = totalItems * avgItemSize

    val scrollY = (firstItem.index * avgItemSize) - firstItem.offset

    val viewportHeight = layoutInfo.viewportSize.height.toFloat()

    val scrollableHeight = (totalContentHeight - viewportHeight).coerceAtLeast(0f)

    val progress = if (scrollableHeight > 0f) {
        (scrollY / scrollableHeight).coerceIn(0f, 1f)
    } else {
        0f
    }

    val thumbHeightPx = with(density) { thumbHeight.toPx() }

    val availableHeight = (viewportHeight - thumbHeightPx - bottomPadding).coerceAtLeast(0f)

    return (progress * availableHeight).coerceIn(0f, availableHeight)
}

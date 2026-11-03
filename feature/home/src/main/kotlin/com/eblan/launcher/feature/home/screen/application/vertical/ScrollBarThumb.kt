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
package com.eblan.launcher.feature.home.screen.application.vertical

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.launch
import kotlin.math.ceil
import kotlin.math.roundToInt

@Composable
internal fun ScrollBarThumb(
    // State and Scrolling
    lazyGridState: LazyGridState,
    canScroll: Boolean,
    // Layout Configuration
    appDrawerColumns: Int,
    // Privacy and Modifiers
    isPrivateSpaceEntryPointHidden: Boolean,
    modifier: Modifier = Modifier,
    // Callbacks
) {
    val density = LocalDensity.current

    val scope = rememberCoroutineScope()

    val thumbHeight by remember(lazyGridState) {
        derivedStateOf {
            with(density) {
                (lazyGridState.layoutInfo.viewportSize.height / 4).toDp()
            }
        }
    }

    val headerHeightPx by rememberHasPrivateSpaceStickyHeader(
        // State
        lazyGridState = lazyGridState,
        // Configuration
        isPrivateSpaceEntryPointHidden = isPrivateSpaceEntryPointHidden,
    )

    val viewPortThumbY by remember(key1 = lazyGridState) {
        derivedStateOf {
            getViewPortThumbY(
                // Grid State
                lazyGridState = lazyGridState,
                // Layout Dimensions
                appDrawerColumns = appDrawerColumns,
                headerHeightPx = headerHeightPx,
                thumbHeight = thumbHeight,
                // Display Metrics
                density = density,
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

    Box(
        modifier = modifier
            .width(10.dp)
            .fillMaxHeight(),
    ) {
        if (!canScroll) return@Box

        Box(
            modifier = Modifier
                .matchParentSize()
                .background(
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f),
                    shape = RoundedCornerShape(10.dp),
                )
                .pointerInput(lazyGridState) {
                    detectTapGestures(
                        onTap = {
                            scrollToTap(
                                // State and Scope
                                lazyGridState = lazyGridState,
                                scope = scope,
                                // Layout and Dimensions
                                appDrawerColumns = appDrawerColumns,
                                density = density,
                                thumbHeight = thumbHeight,
                                // Target
                                offset = it,
                                // Action
                            )
                        },
                    )
                },
            contentAlignment = Alignment.TopCenter,
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
                    )
                    .pointerInput(key1 = lazyGridState) {
                        detectDragGestures(
                            onDragStart = {
                                thumbY = viewPortThumbY

                                isDraggingThumb = true
                            },
                            onDrag = { _, dragAmount ->
                                scrollToDrag(
                                    // State and Scope
                                    lazyGridState = lazyGridState,
                                    scope = scope,
                                    // Layout and Dimensions
                                    appDrawerColumns = appDrawerColumns,
                                    headerHeightPx = headerHeightPx,
                                    thumbHeight = thumbHeight,
                                    density = density,
                                    // Scroll and Position
                                    deltaY = dragAmount.y,
                                    thumbY = thumbY,
                                    // Callbacks
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

@Composable
private fun rememberHasPrivateSpaceStickyHeader(
    // State
    lazyGridState: LazyGridState,
    // Configuration
    isPrivateSpaceEntryPointHidden: Boolean,
): State<Int> = produceState(
    initialValue = 0,
    key1 = lazyGridState,
    key2 = isPrivateSpaceEntryPointHidden,
) {
    if (isPrivateSpaceEntryPointHidden) {
        snapshotFlow {
            lazyGridState.layoutInfo.visibleItemsInfo
                .firstOrNull { it.index == 0 }
                ?.size
                ?.height
        }
            .filterNotNull()
            .collect { value = it }
    } else {
        value = 0
    }
}

private fun scrollToTap(
    // State and Scope
    lazyGridState: LazyGridState,
    scope: CoroutineScope,
    // Layout and Dimensions
    appDrawerColumns: Int,
    density: Density,
    thumbHeight: Dp,
    // Target
    offset: Offset,
    // Action
) {
    val viewportHeight = lazyGridState.layoutInfo.viewportSize.height

    val thumbHeightPx = with(density) { thumbHeight.roundToPx() }

    val maxThumbY = (viewportHeight - thumbHeightPx).coerceAtLeast(0)

    val targetThumbY = (offset.y - thumbHeightPx / 2f)
        .coerceIn(0f, maxThumbY.toFloat())

    val totalRows = ceil(
        lazyGridState.layoutInfo.totalItemsCount / appDrawerColumns.toFloat(),
    ).toInt()

    val row = targetThumbY / maxThumbY.coerceAtLeast(1) * (totalRows - 1)

    val targetIndex = (row.roundToInt() * appDrawerColumns)
        .coerceAtMost(lazyGridState.layoutInfo.totalItemsCount - 1)

    scope.launch {
        lazyGridState.scrollToItem(targetIndex, 0)
    }
}

private fun scrollToDrag(
    // State and Scope
    lazyGridState: LazyGridState,
    scope: CoroutineScope,
    // Layout and Dimensions
    appDrawerColumns: Int,
    headerHeightPx: Int,
    thumbHeight: Dp,
    density: Density,
    // Scroll and Position
    deltaY: Float,
    thumbY: Float,
    // Callbacks
    onUpdateThumbY: (Float) -> Unit,
) {
    if (deltaY == 0f) return

    val layoutInfo = lazyGridState.layoutInfo

    val visibleItems = layoutInfo.visibleItemsInfo

    val totalItems = layoutInfo.totalItemsCount

    val hasHeader = headerHeightPx > 0

    val appItems = visibleItems.filter { it.index != 0 || !hasHeader }

    val avgItemHeight = appItems.map { it.size.height }.average().toFloat()

    val totalRows = (totalItems + appDrawerColumns - 1) / appDrawerColumns

    val appRows = if (hasHeader) {
        (totalItems - 1 + appDrawerColumns - 1) / appDrawerColumns
    } else {
        totalRows
    }

    val viewportHeight = layoutInfo.viewportSize.height

    val thumbHeightPx = with(density) { thumbHeight.toPx() }

    val availableHeight = (viewportHeight - thumbHeightPx).coerceAtLeast(0f)

    val newThumbY = (thumbY + deltaY).coerceIn(0f, availableHeight)

    val progress = if (availableHeight > 0f) newThumbY / availableHeight else 0f

    val totalContentHeight = headerHeightPx + appRows * avgItemHeight + layoutInfo.afterContentPadding

    val scrollableHeight = (
        totalContentHeight - viewportHeight
        ).coerceAtLeast(0f)
    val targetScrollY = (progress * scrollableHeight).coerceIn(0f, scrollableHeight)

    val (targetIndex, targetScrollOffset) = if (hasHeader && targetScrollY >= headerHeightPx) {
        val targetRow = ((targetScrollY - headerHeightPx) / avgItemHeight)
            .coerceIn(0f, (appRows - 1).coerceAtLeast(0).toFloat())
        val rowIndex = targetRow.toInt()
        val targetScrollOffset = ((targetRow - rowIndex) * avgItemHeight).toInt()
        (1 + rowIndex * appDrawerColumns).coerceIn(1, totalItems - 1) to targetScrollOffset
    } else if (hasHeader) {
        0 to targetScrollY.toInt()
    } else {
        val targetRow = (targetScrollY / avgItemHeight)
            .coerceIn(0f, (appRows - 1).coerceAtLeast(0).toFloat())
        val rowIndex = targetRow.toInt()
        val targetScrollOffset = ((targetRow - rowIndex) * avgItemHeight).toInt()
        (rowIndex * appDrawerColumns).coerceIn(0, totalItems - 1) to targetScrollOffset
    }

    onUpdateThumbY(newThumbY)

    scope.launch {
        lazyGridState.scrollToItem(targetIndex, targetScrollOffset)
    }
}

private fun getViewPortThumbY(
    // Grid State
    lazyGridState: LazyGridState,
    // Layout Dimensions
    appDrawerColumns: Int,
    headerHeightPx: Int,
    thumbHeight: Dp,
    // Display Metrics
    density: Density,
): Float {
    val layoutInfo = lazyGridState.layoutInfo
    val visibleItemsInfo = layoutInfo.visibleItemsInfo

    val totalItemsCount = layoutInfo.totalItemsCount

    val totalRows = (totalItemsCount + appDrawerColumns - 1) / appDrawerColumns

    val hasHeader = headerHeightPx > 0

    val appItems = visibleItemsInfo.filter { it.index != 0 || !hasHeader }

    val avgItemHeight = appItems.map { it.size.height }.average().toFloat()

    val appRows = if (hasHeader) {
        (totalItemsCount - 1 + appDrawerColumns - 1) / appDrawerColumns
    } else {
        totalRows
    }

    val viewportHeight = layoutInfo.viewportSize.height.toFloat()

    val totalContentHeight = headerHeightPx + appRows * avgItemHeight + layoutInfo.afterContentPadding

    val firstItem = appItems.firstOrNull()

    val scrollY = if (firstItem == null) {
        0f
    } else if (hasHeader) {
        headerHeightPx +
            (((firstItem.index - 1) / appDrawerColumns) * avgItemHeight) -
            firstItem.offset.y
    } else {
        ((firstItem.index / appDrawerColumns) * avgItemHeight) - firstItem.offset.y
    }

    val scrollableHeight = (
        totalContentHeight - viewportHeight
        ).coerceAtLeast(0f)

    val progress = if (scrollableHeight > 0f) {
        (scrollY / scrollableHeight).coerceIn(0f, 1f)
    } else {
        0f
    }

    val thumbHeightPx = with(density) { thumbHeight.toPx() }

    val availableHeight = (viewportHeight - thumbHeightPx)
        .coerceAtLeast(0f)

    return (progress * availableHeight)
        .coerceIn(0f, availableHeight)
}

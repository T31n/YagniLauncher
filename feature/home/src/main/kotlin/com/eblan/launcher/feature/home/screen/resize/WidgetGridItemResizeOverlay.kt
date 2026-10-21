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
package com.eblan.launcher.feature.home.screen.resize

import android.appwidget.AppWidgetProviderInfo
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.VectorConverter
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.eblan.launcher.domain.grid.isGridItemSpanWithinBounds
import com.eblan.launcher.domain.grid.resizeWidgetGridItemWithPixels
import com.eblan.launcher.domain.model.grid.GridItem
import com.eblan.launcher.domain.model.grid.GridItemData
import com.eblan.launcher.domain.model.grid.SideAnchor
import com.eblan.launcher.feature.home.component.dashedResizeOutline
import com.eblan.launcher.feature.home.component.resizeHandleModifier
import com.eblan.launcher.feature.home.util.DRAG_HANDLE_SIZE
import com.eblan.launcher.feature.home.util.updateAppWidgetOptions
import com.eblan.launcher.framework.widgetmanager.AndroidAppWidgetManagerWrapper
import com.eblan.launcher.ui.local.LocalAppWidgetManager
import kotlinx.coroutines.launch
import kotlin.Int
import kotlin.math.roundToInt

@Composable
internal fun WidgetGridItemResizeOverlay(
    // Item Data
    data: GridItemData.Widget,
    gridItem: GridItem,
    // Dimensions
    columns: Int,
    rows: Int,
    gridWidth: Int,
    gridHeight: Int,
    width: Int,
    height: Int,
    // Position and Style
    x: Int,
    y: Int,
    color: Color,
    lockMovement: Boolean,
    modifier: Modifier = Modifier,
    // Callback
    onResizeWidgetGridItem: (
        gridItem: GridItem,
        columns: Int,
        rows: Int,
    ) -> Unit
) {
    val density = LocalDensity.current

    val scope = rememberCoroutineScope()

    val appWidgetManager = LocalAppWidgetManager.current

    val currentX = remember {
        Animatable(
            initialValue = x,
            typeConverter = Int.VectorConverter,
        )
    }

    val currentY = remember {
        Animatable(
            initialValue = y,
            typeConverter = Int.VectorConverter,
        )
    }

    val currentWidth = remember {
        Animatable(
            initialValue = width,
            typeConverter = Int.VectorConverter,
        )
    }

    val currentHeight = remember {
        Animatable(
            initialValue = height,
            typeConverter = Int.VectorConverter,
        )
    }

    var isResizing by remember {
        mutableStateOf(true)
    }

    var dragHandle by remember { mutableStateOf(Alignment.Center) }

    val dragHandleSizePx = with(density) {
        DRAG_HANDLE_SIZE.roundToPx()
    }

    val borderWidth by remember {
        derivedStateOf {
            with(density) {
                currentWidth.value.coerceAtLeast(dragHandleSizePx).toDp()
            }
        }
    }

    val borderHeight by remember {
        derivedStateOf {
            with(density) {
                currentHeight.value.coerceAtLeast(dragHandleSizePx).toDp()
            }
        }
    }

    val borderX by remember {
        derivedStateOf {
            getWidgetBorderX(
                // Position
                x = x,
                currentX = currentX.value,
                // Dimensions
                width = width,
                currentWidth = currentWidth.value,
                // DragHandle
                dragHandle = dragHandle,
                dragHandleSizePx = dragHandleSizePx
            )
        }
    }

    val borderY by remember {
        derivedStateOf {
            getWidgetBorderY(
                // Position
                y = y,
                currentY = currentY.value,
                // Dimensions
                height = height,
                currentHeight = currentHeight.value,
                // Drag Configuration
                dragHandle = dragHandle,
                dragHandleSizePx = dragHandleSizePx
            )
        }
    }

    LaunchedEffect(
        key1 = currentWidth.value,
        key2 = currentHeight.value,
    ) {
        resizeWidgetGridItem(
            // Widget Data and Manager
            data = data,
            appWidgetManager = appWidgetManager,
            // Grid Item and Dimensions
            gridItem = gridItem,
            gridWidth = gridWidth,
            gridHeight = gridHeight,
            currentWidth = currentWidth.value,
            currentHeight = currentHeight.value,
            rows = rows,
            columns = columns,
            width = width,
            height = height,
            // Interaction and Layout
            dragHandle = dragHandle,
            isResizing = isResizing,
            lockMovement = lockMovement,
            density = density,
            // Callback
            onResizeWidgetGridItem = onResizeWidgetGridItem
        )
    }

    LaunchedEffect(key1 = isResizing) {
        if (!isResizing) {
            launch { currentX.animateTo(targetValue = x) }
            launch { currentY.animateTo(targetValue = y) }
            launch { currentWidth.animateTo(targetValue = width) }
            launch { currentHeight.animateTo(targetValue = height) }
        }
    }

    Box(
        modifier = modifier
            .offset {
                IntOffset(
                    x = borderX,
                    y = borderY,
                )
            }
            .size(width = borderWidth, height = borderHeight)
            .background(
                color = color.copy(alpha = 0.08f),
                shape = RoundedCornerShape(12.dp),
            )
            .dashedResizeOutline(color),
    ) {
        Box(
            modifier = Modifier.run {
                if (data.resizeMode == AppWidgetProviderInfo.RESIZE_VERTICAL || data.resizeMode == AppWidgetProviderInfo.RESIZE_BOTH) {
                    align(Alignment.TopCenter)
                        .offset(y = (-15).dp)
                        .resizeHandleModifier(color = color)
                        .pointerInput(Unit) {
                            detectDragGestures(
                                onDragStart = {
                                    dragHandle = Alignment.TopCenter

                                    isResizing = true
                                },
                                onDragEnd = {
                                    isResizing = false
                                },
                                onDrag = { _, dragAmount ->
                                    scope.launch {
                                        currentHeight.snapTo((currentHeight.value - dragAmount.y).roundToInt())
                                        currentY.snapTo((currentY.value + dragAmount.y).roundToInt())
                                    }
                                },
                            )
                        }
                } else {
                    this
                }
            },
        )

        Box(
            modifier = Modifier.run {
                if (data.resizeMode == AppWidgetProviderInfo.RESIZE_HORIZONTAL || data.resizeMode == AppWidgetProviderInfo.RESIZE_BOTH) {
                    align(Alignment.CenterEnd)
                        .offset(15.dp)
                        .resizeHandleModifier(color = color)
                        .pointerInput(Unit) {
                            detectDragGestures(
                                onDragStart = {
                                    dragHandle = Alignment.CenterEnd

                                    isResizing = true
                                },
                                onDragEnd = {
                                    isResizing = false
                                },
                                onDrag = { _, dragAmount ->
                                    scope.launch {
                                        currentWidth.snapTo((currentWidth.value + dragAmount.x).roundToInt())
                                    }
                                },
                            )
                        }
                } else {
                    this
                }
            },
        )

        Box(
            modifier = Modifier.run {
                if (data.resizeMode == AppWidgetProviderInfo.RESIZE_VERTICAL || data.resizeMode == AppWidgetProviderInfo.RESIZE_BOTH) {
                    align(Alignment.BottomCenter)
                        .offset(y = 15.dp)
                        .resizeHandleModifier(color = color)
                        .pointerInput(Unit) {
                            detectDragGestures(
                                onDragStart = {
                                    dragHandle = Alignment.BottomCenter

                                    isResizing = true
                                },
                                onDragEnd = {
                                    isResizing = false
                                },
                                onDrag = { _, dragAmount ->
                                    scope.launch {
                                        currentHeight.snapTo((currentHeight.value + dragAmount.y).roundToInt())
                                    }
                                },
                            )
                        }
                } else {
                    this
                }
            },
        )

        Box(
            modifier = Modifier.run {
                if (data.resizeMode == AppWidgetProviderInfo.RESIZE_HORIZONTAL || data.resizeMode == AppWidgetProviderInfo.RESIZE_BOTH) {
                    align(Alignment.CenterStart)
                        .offset((-15).dp)
                        .resizeHandleModifier(color = color)
                        .pointerInput(Unit) {
                            detectDragGestures(
                                onDragStart = {
                                    dragHandle = Alignment.CenterStart

                                    isResizing = true
                                },
                                onDragEnd = {
                                    isResizing = false
                                },
                                onDrag = { _, dragAmount ->
                                    scope.launch {
                                        currentWidth.snapTo((currentWidth.value - dragAmount.x).roundToInt())
                                        currentX.snapTo((currentX.value + dragAmount.x).roundToInt())
                                    }
                                },
                            )
                        }
                } else {
                    this
                }
            },
        )
    }
}

private fun getWidgetBorderX(
    // Position
    x: Int,
    currentX: Int,
    // Dimensions
    width: Int,
    currentWidth: Int,
    // DragHandle
    dragHandle: Alignment,
    dragHandleSizePx: Int
): Int = if (dragHandle == Alignment.CenterStart) {
    if (currentWidth >= dragHandleSizePx) {
        currentX
    } else {
        (x + width) - dragHandleSizePx
    }
} else {
    currentX
}

private fun getWidgetBorderY(
    // Position
    y: Int,
    currentY: Int,
    // Dimensions
    height: Int,
    currentHeight: Int,
    // Drag Configuration
    dragHandle: Alignment,
    dragHandleSizePx: Int
): Int = if (dragHandle == Alignment.TopCenter) {
    if (currentHeight >= dragHandleSizePx) {
        currentY
    } else {
        (y + height) - dragHandleSizePx
    }
} else {
    currentY
}

private fun resizeWidgetGridItem(
    // Widget Data and Manager
    data: GridItemData.Widget,
    appWidgetManager: AndroidAppWidgetManagerWrapper,
    // Grid Item and Dimensions
    gridItem: GridItem,
    gridWidth: Int,
    gridHeight: Int,
    currentWidth: Int,
    currentHeight: Int,
    rows: Int,
    columns: Int,
    width: Int,
    height: Int,
    // Interaction and Layout
    dragHandle: Alignment,
    isResizing: Boolean,
    lockMovement: Boolean,
    density: Density,
    // Callback
    onResizeWidgetGridItem: (GridItem, Int, Int) -> Unit
) {
    val minResizeWidthPx = with(density) { data.minResizeWidth.dp.roundToPx() }
    val minResizeHeightPx = with(density) { data.minResizeHeight.dp.roundToPx() }
    val maxResizeWidthPx = with(density) { data.maxResizeWidth.dp.roundToPx() }
    val maxResizeHeightPx = with(density) { data.maxResizeHeight.dp.roundToPx() }

    val allowedWidth =
        if (minResizeWidthPx > 0 && currentWidth <= minResizeWidthPx) {
            minResizeWidthPx
        } else if (maxResizeWidthPx in 1..<currentWidth) {
            maxResizeWidthPx
        } else {
            currentWidth
        }

    val allowedHeight =
        if (minResizeHeightPx > 0 && currentHeight <= minResizeHeightPx) {
            minResizeHeightPx
        } else if (maxResizeHeightPx in 1..<currentHeight) {
            maxResizeHeightPx
        } else {
            currentHeight
        }

    val resizingGridItem = when (dragHandle) {
        Alignment.TopCenter -> {
            resizeWidgetGridItemWithPixels(
                gridItem = gridItem,
                width = width,
                height = allowedHeight,
                rows = rows,
                columns = columns,
                gridWidth = gridWidth,
                gridHeight = gridHeight,
                anchor = SideAnchor.Bottom,
            )
        }

        Alignment.CenterEnd -> {
            resizeWidgetGridItemWithPixels(
                gridItem = gridItem,
                width = allowedWidth,
                height = height,
                rows = rows,
                columns = columns,
                gridWidth = gridWidth,
                gridHeight = gridHeight,
                anchor = SideAnchor.Left,
            )
        }

        Alignment.BottomCenter -> {
            resizeWidgetGridItemWithPixels(
                gridItem = gridItem,
                width = width,
                height = allowedHeight,
                rows = rows,
                columns = columns,
                gridWidth = gridWidth,
                gridHeight = gridHeight,
                anchor = SideAnchor.Top,
            )
        }

        Alignment.CenterStart -> {
            resizeWidgetGridItemWithPixels(
                gridItem = gridItem,
                width = allowedWidth,
                height = height,
                rows = rows,
                columns = columns,
                gridWidth = gridWidth,
                gridHeight = gridHeight,
                anchor = SideAnchor.Right,
            )
        }

        else -> null
    }

    if (isResizing && resizingGridItem != null && isGridItemSpanWithinBounds(
            gridItem = resizingGridItem,
            columns = columns,
            rows = rows,
        ) && !lockMovement
    ) {
        updateAppWidgetOptions(
            // Manager
            androidAppWidgetManagerWrapper = appWidgetManager,
            // Data
            data = data,
            // Dimensions
            width = allowedWidth,
            height = allowedHeight,
            density = density,
            columns = columns,
            rows = rows,
            gridWidth = gridWidth,
            gridHeight = gridHeight,
            startColumn = resizingGridItem.startColumn,
            startRow = resizingGridItem.startRow
        )

        onResizeWidgetGridItem(
            resizingGridItem,
            columns,
            rows,
        )
    }
}

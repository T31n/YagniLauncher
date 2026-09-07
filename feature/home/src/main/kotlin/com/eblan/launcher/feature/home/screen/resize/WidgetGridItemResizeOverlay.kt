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
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
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
import com.eblan.launcher.domain.model.GridItem
import com.eblan.launcher.domain.model.GridItemData
import com.eblan.launcher.domain.model.SideAnchor
import com.eblan.launcher.feature.home.util.DRAG_HANDLE_SIZE
import com.eblan.launcher.feature.home.util.updateAppWidgetOptions
import com.eblan.launcher.framework.widgetmanager.AndroidAppWidgetManagerWrapper
import com.eblan.launcher.ui.local.LocalAppWidgetManager
import kotlinx.coroutines.launch
import kotlin.Int
import kotlin.math.roundToInt

@Composable
internal fun WidgetGridItemResizeOverlay(
    modifier: Modifier = Modifier,
    color: Color,
    columns: Int,
    data: GridItemData.Widget,
    gridHeight: Int,
    gridItem: GridItem,
    gridWidth: Int,
    height: Int,
    lockMovement: Boolean,
    rows: Int,
    width: Int,
    x: Int,
    y: Int,
    onResizeWidgetGridItem: (
        gridItem: GridItem,
        columns: Int,
        rows: Int,
    ) -> Unit,
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
                dragHandle = dragHandle,
                currentWidth = currentWidth.value,
                dragHandleSizePx = dragHandleSizePx,
                currentX = currentX.value,
                x = x,
                width = width,
            )
        }
    }

    val borderY by remember {
        derivedStateOf {
            getWidgetBorderY(
                dragHandle = dragHandle,
                currentHeight = currentHeight.value,
                dragHandleSizePx = dragHandleSizePx,
                currentY = currentY.value,
                y = y,
                height = height,
            )
        }
    }

    val circleModifier =
        Modifier
            .size(DRAG_HANDLE_SIZE)
            .background(color = color, shape = CircleShape)

    LaunchedEffect(
        key1 = currentWidth.value,
        key2 = currentHeight.value,
    ) {
        resizeWidgetGridItem(
            data = data,
            currentWidth = currentWidth.value,
            currentHeight = currentHeight.value,
            dragHandle = dragHandle,
            gridItem = gridItem,
            width = width,
            rows = rows,
            columns = columns,
            gridWidth = gridWidth,
            gridHeight = gridHeight,
            height = height,
            isResizing = isResizing,
            lockMovement = lockMovement,
            appWidgetManager = appWidgetManager,
            density = density,
            onResizeWidgetGridItem = onResizeWidgetGridItem,
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
            .border(width = 2.dp, color = color),
    ) {
        Box(
            modifier = Modifier.run {
                if (data.resizeMode == AppWidgetProviderInfo.RESIZE_VERTICAL || data.resizeMode == AppWidgetProviderInfo.RESIZE_BOTH) {
                    align(Alignment.TopCenter)
                        .offset(y = (-15).dp)
                        .then(circleModifier)
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
                                        currentHeight.snapTo(currentHeight.value - dragAmount.y.roundToInt())
                                        currentY.snapTo(currentY.value + dragAmount.y.roundToInt())
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
                        .then(circleModifier)
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
                                        currentWidth.snapTo(currentWidth.value + dragAmount.x.roundToInt())
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
                        .then(circleModifier)
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
                                        currentHeight.snapTo(currentHeight.value + dragAmount.y.roundToInt())
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
                        .then(circleModifier)
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
                                        currentWidth.snapTo(currentWidth.value - dragAmount.x.roundToInt())
                                        currentX.snapTo(currentX.value + dragAmount.x.roundToInt())
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
    dragHandle: Alignment,
    currentWidth: Int,
    dragHandleSizePx: Int,
    currentX: Int,
    x: Int,
    width: Int,
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
    dragHandle: Alignment,
    currentHeight: Int,
    dragHandleSizePx: Int,
    currentY: Int,
    y: Int,
    height: Int,
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
    data: GridItemData.Widget,
    currentWidth: Int,
    currentHeight: Int,
    dragHandle: Alignment,
    gridItem: GridItem,
    width: Int,
    rows: Int,
    columns: Int,
    gridWidth: Int,
    gridHeight: Int,
    height: Int,
    isResizing: Boolean,
    lockMovement: Boolean,
    appWidgetManager: AndroidAppWidgetManagerWrapper,
    density: Density,
    onResizeWidgetGridItem: (GridItem, Int, Int) -> Unit,
) {
    val allowedWidth =
        if (data.minResizeWidth > 0 && currentWidth <= data.minResizeWidth) {
            data.minResizeWidth
        } else if (data.maxResizeWidth in 1..<currentWidth) {
            data.maxResizeWidth
        } else {
            currentWidth
        }

    val allowedHeight =
        if (data.minResizeHeight > 0 && currentHeight <= data.minResizeHeight) {
            data.minResizeHeight
        } else if (data.maxResizeHeight in 1..<currentHeight) {
            data.maxResizeHeight
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
            height = allowedHeight,
            width = allowedWidth,
            androidAppWidgetManagerWrapper = appWidgetManager,
            columns = columns,
            data = data,
            density = density,
            gridHeight = gridHeight,
            gridWidth = gridWidth,
            rows = rows,
            startColumn = resizingGridItem.startColumn,
            startRow = resizingGridItem.startRow,
        )

        onResizeWidgetGridItem(
            resizingGridItem,
            columns,
            rows,
        )
    }
}

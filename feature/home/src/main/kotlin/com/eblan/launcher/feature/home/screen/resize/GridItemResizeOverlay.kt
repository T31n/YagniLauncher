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
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.eblan.launcher.domain.grid.isGridItemSpanWithinBounds
import com.eblan.launcher.domain.grid.resizeGridItemWithPixels
import com.eblan.launcher.domain.model.grid.Anchor
import com.eblan.launcher.domain.model.grid.GridItem
import com.eblan.launcher.feature.home.component.dashedResizeOutline
import com.eblan.launcher.feature.home.component.resizeHandleModifier
import com.eblan.launcher.feature.home.util.DRAG_HANDLE_SIZE
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@Composable
internal fun GridItemResizeOverlay(
    // Grid Item
    gridItem: GridItem,
    // Grid Dimensions
    columns: Int,
    rows: Int,
    gridWidth: Int,
    gridHeight: Int,
    // Cell Dimensions
    cellWidth: Int,
    cellHeight: Int,
    // Position and Size
    x: Int,
    y: Int,
    width: Int,
    height: Int,
    // Appearance and Behavior
    color: Color,
    lockMovement: Boolean,
    modifier: Modifier = Modifier,
    // Callbacks
    onResizeGridItem: (
        gridItem: GridItem,
        columns: Int,
        rows: Int,
    ) -> Unit
) {
    val density = LocalDensity.current

    val scope = rememberCoroutineScope()

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
            getBorderX(
                // Position
                currentX = currentX.value,
                x = x,
                // Dimensions
                currentWidth = currentWidth.value,
                width = width,
                // Drag Configuration
                dragHandle = dragHandle,
                dragHandleSizePx = dragHandleSizePx
            )
        }
    }

    val borderY by remember {
        derivedStateOf {
            getBorderY(
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
        resizeGridItem(
            // Item and State
            gridItem = gridItem,
            dragHandle = dragHandle,
            isResizing = isResizing,
            lockMovement = lockMovement,
            // Dimensions
            currentWidth = currentWidth.value,
            currentHeight = currentHeight.value,
            cellWidth = cellWidth,
            cellHeight = cellHeight,
            gridWidth = gridWidth,
            gridHeight = gridHeight,
            columns = columns,
            rows = rows,
            // Callback
            onResizeGridItem = onResizeGridItem
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
            .size(
                width = borderWidth,
                height = borderHeight,
            )
            .background(
                color = color.copy(alpha = 0.08f),
                shape = RoundedCornerShape(12.dp),
            )
            .dashedResizeOutline(color),
    ) {
        Box(
            modifier = Modifier
                .align(Alignment.TopStart)
                .offset((-15).dp, (-15).dp)
                .resizeHandleModifier(color = color)
                .pointerInput(Unit) {
                    detectDragGestures(
                        onDragStart = {
                            dragHandle = Alignment.TopStart

                            isResizing = true
                        },
                        onDragEnd = {
                            isResizing = false
                        },
                        onDrag = { _, dragAmount ->
                            scope.launch {
                                currentWidth.snapTo((currentWidth.value - dragAmount.x).roundToInt())

                                currentHeight.snapTo((currentHeight.value - dragAmount.y).roundToInt())

                                currentX.snapTo((currentX.value + dragAmount.x).roundToInt())

                                currentY.snapTo((currentY.value + dragAmount.y).roundToInt())
                            }
                        },
                    )
                },
        )

        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .offset(15.dp, (-15).dp)
                .resizeHandleModifier(color = color)
                .pointerInput(Unit) {
                    detectDragGestures(
                        onDragStart = {
                            dragHandle = Alignment.TopEnd

                            isResizing = true
                        },
                        onDragEnd = {
                            isResizing = false
                        },
                        onDrag = { _, dragAmount ->
                            scope.launch {
                                currentWidth.snapTo((currentWidth.value + dragAmount.x).roundToInt())

                                currentHeight.snapTo((currentHeight.value - dragAmount.y).roundToInt())

                                currentY.snapTo((currentY.value + dragAmount.y).roundToInt())
                            }
                        },
                    )
                },
        )

        Box(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .offset((-15).dp, 15.dp)
                .resizeHandleModifier(color = color)
                .pointerInput(Unit) {
                    detectDragGestures(
                        onDragStart = {
                            dragHandle = Alignment.BottomStart

                            isResizing = true
                        },
                        onDragEnd = {
                            isResizing = false
                        },
                        onDrag = { _, dragAmount ->
                            scope.launch {
                                currentWidth.snapTo((currentWidth.value - dragAmount.x).roundToInt())

                                currentHeight.snapTo((currentHeight.value + dragAmount.y).roundToInt())

                                currentX.snapTo((currentX.value + dragAmount.x).roundToInt())
                            }
                        },
                    )
                },
        )

        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .offset(15.dp, 15.dp)
                .resizeHandleModifier(color = color)
                .pointerInput(Unit) {
                    detectDragGestures(
                        onDragStart = {
                            dragHandle = Alignment.BottomEnd

                            isResizing = true
                        },
                        onDragEnd = {
                            isResizing = false
                        },
                        onDrag = { _, dragAmount ->
                            scope.launch {
                                currentWidth.snapTo((currentWidth.value + dragAmount.x).roundToInt())

                                currentHeight.snapTo((currentHeight.value + dragAmount.y).roundToInt())
                            }
                        },
                    )
                },
        )
    }
}

private fun getBorderY(
    // Position
    y: Int,
    currentY: Int,
    // Dimensions
    height: Int,
    currentHeight: Int,
    // Drag Configuration
    dragHandle: Alignment,
    dragHandleSizePx: Int
): Int = if (currentHeight >= dragHandleSizePx) {
    currentY
} else if (dragHandle == Alignment.TopStart || dragHandle == Alignment.TopEnd) {
    (y + height) - dragHandleSizePx
} else {
    y
}

private fun getBorderX(
    // Position
    currentX: Int,
    x: Int,
    // Dimensions
    currentWidth: Int,
    width: Int,
    // Drag Configuration
    dragHandle: Alignment,
    dragHandleSizePx: Int
): Int = when (dragHandle) {
    Alignment.TopStart -> {
        if (currentWidth >= dragHandleSizePx) {
            currentX
        } else {
            (x + width) - dragHandleSizePx
        }
    }

    Alignment.TopEnd -> {
        if (currentWidth >= dragHandleSizePx) {
            currentX
        } else {
            x
        }
    }

    Alignment.BottomStart -> {
        if (currentWidth >= dragHandleSizePx) {
            currentX
        } else {
            (x + width) - dragHandleSizePx
        }
    }

    else -> {
        if (currentWidth >= dragHandleSizePx) {
            currentX
        } else {
            x
        }
    }
}

private fun resizeGridItem(
    // Item and State
    gridItem: GridItem,
    dragHandle: Alignment,
    isResizing: Boolean,
    lockMovement: Boolean,
    // Dimensions
    currentWidth: Int,
    currentHeight: Int,
    cellWidth: Int,
    cellHeight: Int,
    gridWidth: Int,
    gridHeight: Int,
    columns: Int,
    rows: Int,
    // Callback
    onResizeGridItem: (GridItem, Int, Int) -> Unit
) {
    val allowedWidth = currentWidth.coerceAtLeast(cellWidth)

    val allowedHeight = currentHeight.coerceAtLeast(cellHeight)

    val resizingGridItem = when (dragHandle) {
        Alignment.TopStart -> {
            resizeGridItemWithPixels(
                gridItem = gridItem,
                width = allowedWidth,
                height = allowedHeight,
                rows = rows,
                columns = columns,
                gridWidth = gridWidth,
                gridHeight = gridHeight,
                anchor = Anchor.BottomEnd,
            )
        }

        Alignment.TopEnd -> {
            resizeGridItemWithPixels(
                gridItem = gridItem,
                width = allowedWidth,
                height = allowedHeight,
                rows = rows,
                columns = columns,
                gridWidth = gridWidth,
                gridHeight = gridHeight,
                anchor = Anchor.BottomStart,
            )
        }

        Alignment.BottomStart -> {
            resizeGridItemWithPixels(
                gridItem = gridItem,
                width = allowedWidth,
                height = allowedHeight,
                rows = rows,
                columns = columns,
                gridWidth = gridWidth,
                gridHeight = gridHeight,
                anchor = Anchor.TopEnd,
            )
        }

        Alignment.BottomEnd -> {
            resizeGridItemWithPixels(
                gridItem = gridItem,
                width = allowedWidth,
                height = allowedHeight,
                rows = rows,
                columns = columns,
                gridWidth = gridWidth,
                gridHeight = gridHeight,
                anchor = Anchor.TopStart,
            )
        }

        else -> null
    }

    if (isResizing &&
        resizingGridItem != null &&
        isGridItemSpanWithinBounds(
            gridItem = resizingGridItem,
            columns = columns,
            rows = rows,
        ) && !lockMovement
    ) {
        onResizeGridItem(
            resizingGridItem,
            columns,
            rows,
        )
    }
}

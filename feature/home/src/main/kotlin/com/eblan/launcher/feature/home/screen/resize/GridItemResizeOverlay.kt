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
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.eblan.launcher.domain.grid.isGridItemSpanWithinBounds
import com.eblan.launcher.domain.grid.resizeGridItemWithPixels
import com.eblan.launcher.domain.model.Anchor
import com.eblan.launcher.domain.model.GridItem
import com.eblan.launcher.feature.home.util.DRAG_HANDLE_SIZE
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@Composable
internal fun GridItemResizeOverlay(
    modifier: Modifier = Modifier,
    cellHeight: Int,
    cellWidth: Int,
    color: Color,
    columns: Int,
    gridHeight: Int,
    gridItem: GridItem,
    gridWidth: Int,
    height: Int,
    lockMovement: Boolean,
    rows: Int,
    width: Int,
    x: Int,
    y: Int,
    onResizeGridItem: (
        gridItem: GridItem,
        columns: Int,
        rows: Int,
    ) -> Unit,
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
            getBorderY(
                dragHandle = dragHandle,
                currentHeight = currentHeight.value,
                dragHandleSizePx = dragHandleSizePx,
                currentY = currentY.value,
                y = y,
                height = height,
            )
        }
    }

    val circleModifier = Modifier
        .size(DRAG_HANDLE_SIZE)
        .background(color = color, shape = CircleShape)

    LaunchedEffect(
        key1 = currentWidth.value,
        key2 = currentHeight.value,
    ) {
        resizeGridItem(
            currentWidth = currentWidth.value,
            cellWidth = cellWidth,
            currentHeight = currentHeight.value,
            cellHeight = cellHeight,
            dragHandle = dragHandle,
            gridItem = gridItem,
            rows = rows,
            columns = columns,
            gridWidth = gridWidth,
            gridHeight = gridHeight,
            isResizing = isResizing,
            lockMovement = lockMovement,
            onResizeGridItem = onResizeGridItem,
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
            .border(width = 2.dp, color = color),
    ) {
        Box(
            modifier = Modifier
                .align(Alignment.TopStart)
                .offset((-15).dp, (-15).dp)
                .then(circleModifier)
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
                .then(circleModifier)
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
                .then(circleModifier)
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
                .then(circleModifier)
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
    dragHandle: Alignment,
    currentHeight: Int,
    dragHandleSizePx: Int,
    currentY: Int,
    y: Int,
    height: Int,
): Int = if (currentHeight >= dragHandleSizePx) {
    currentY
} else if (dragHandle == Alignment.TopStart || dragHandle == Alignment.TopEnd) {
    (y + height) - dragHandleSizePx
} else {
    y
}

private fun getBorderX(
    dragHandle: Alignment,
    currentWidth: Int,
    dragHandleSizePx: Int,
    currentX: Int,
    x: Int,
    width: Int,
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
    currentWidth: Int,
    cellWidth: Int,
    currentHeight: Int,
    cellHeight: Int,
    dragHandle: Alignment,
    gridItem: GridItem,
    rows: Int,
    columns: Int,
    gridWidth: Int,
    gridHeight: Int,
    isResizing: Boolean,
    lockMovement: Boolean,
    onResizeGridItem: (GridItem, Int, Int) -> Unit,
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

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

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.VectorConverter
import androidx.compose.animation.core.VisibilityThreshold
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.layout.ParentDataModifier
import androidx.compose.ui.layout.SubcomposeLayout
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.eblan.launcher.domain.model.application.EblanApplicationInfo
import com.eblan.launcher.domain.model.grid.GridItem
import com.eblan.launcher.domain.model.grid.GridItemSettings
import com.eblan.launcher.domain.model.userdata.TextColor
import com.eblan.launcher.feature.home.util.getGridItemTextColor
import kotlin.math.hypot

@Composable
internal fun GridLayout(
    // Grid Properties
    gridItems: List<GridItem>?,
    columns: Int,
    rows: Int,
    // Layout Options
    animate: Boolean,
    modifier: Modifier = Modifier,
    // Content
    content: @Composable BoxScope.(GridItem) -> Unit,
) {
    SubcomposeLayout(modifier = modifier) { constraints ->
        val cellWidth = constraints.maxWidth / columns
        val cellHeight = constraints.maxHeight / rows

        layout(width = constraints.maxWidth, height = constraints.maxHeight) {
            gridItems?.forEach { gridItem ->
                subcompose(gridItem.id) {
                    GridLayoutItem(
                        // Item and Dimensions
                        gridItem = gridItem,
                        cellWidth = cellWidth,
                        cellHeight = cellHeight,
                        // Behavior and Modifier
                        animate = animate,
                        // Content
                        content = content,
                    )
                }.forEach { measurable ->
                    val parentData = measurable.parentData as GridItemParentData

                    measurable.measure(
                        Constraints.fixed(
                            width = parentData.width,
                            height = parentData.height,
                        ),
                    ).placeRelative(
                        x = parentData.x,
                        y = parentData.y,
                    )
                }
            }
        }
    }
}

@Composable
internal fun DotsGridLayout(
    // Grid Structure
    columns: Int,
    rows: Int,
    // Grid Item
    gridItem: GridItem,
    gridItemSettings: GridItemSettings,
    // Drag and Style
    dragIntOffset: IntOffset,
    textColor: TextColor,
    // Modifier
    modifier: Modifier = Modifier,
) {
    var gridPositionInRoot by remember { mutableStateOf(Offset.Zero) }

    val currentGridItemSettings = if (gridItem.override) {
        gridItem.gridItemSettings
    } else {
        gridItemSettings
    }

    val currentTextColor = getGridItemTextColor(
        // Grid Item Text Colors
        gridItemTextColor = currentGridItemSettings.textColor,
        gridItemCustomTextColor = currentGridItemSettings.customTextColor,
        // System Text Colors
        systemTextColor = textColor,
        systemCustomTextColor = gridItemSettings.customTextColor,
    )

    Canvas(
        modifier = modifier.onGloballyPositioned {
            gridPositionInRoot = it.positionInRoot()
        },
    ) {
        val cellWidth = size.width / columns
        val cellHeight = size.height / rows
        val dragPosition = Offset(
            x = dragIntOffset.x - gridPositionInRoot.x,
            y = dragIntOffset.y - gridPositionInRoot.y,
        )
        val fadeRadius = maxOf(cellWidth, cellHeight) * 2.5f

        fun drawDot(center: Offset, radius: Float) {
            val distance = hypot(
                x = (center.x - dragPosition.x).toDouble(),
                y = (center.y - dragPosition.y).toDouble(),
            ).toFloat()
            val alpha = (1f - distance / fadeRadius).coerceIn(0f, 1f) * 0.85f

            if (alpha == 0f) return

            drawCircle(
                color = currentTextColor,
                radius = radius,
                center = center,
                alpha = alpha,
            )
            drawCircle(
                color = currentTextColor,
                radius = radius * 0.55f,
                center = center,
                alpha = alpha,
            )
        }

        val cornerRadius = 4.dp.toPx()
        val midpointRadius = 2.5.dp.toPx()

        for (row in 0..rows) {
            for (column in 0..columns) {
                drawDot(
                    center = Offset(
                        x = column * cellWidth,
                        y = row * cellHeight,
                    ),
                    radius = cornerRadius,
                )
            }
        }

        for (row in 0..rows) {
            for (column in 0 until columns) {
                drawDot(
                    center = Offset(
                        x = (column + 0.5f) * cellWidth,
                        y = row * cellHeight,
                    ),
                    radius = midpointRadius,
                )
            }
        }

        for (row in 0 until rows) {
            for (column in 0..columns) {
                drawDot(
                    center = Offset(
                        x = column * cellWidth,
                        y = (row + 0.5f) * cellHeight,
                    ),
                    radius = midpointRadius,
                )
            }
        }
    }
}

@Composable
internal fun HorizontalAppDrawerGridLayout(
    // Data
    eblanApplicationInfos: List<EblanApplicationInfo>?,
    // Layout
    rows: Int,
    columns: Int,
    // Modifier
    modifier: Modifier = Modifier,
    // Content
    content: @Composable BoxScope.(EblanApplicationInfo) -> Unit,
) {
    SubcomposeLayout(modifier = modifier) { constraints ->
        val cellWidth = constraints.maxWidth / columns
        val cellHeight = constraints.maxHeight / rows

        layout(constraints.maxWidth, constraints.maxHeight) {
            eblanApplicationInfos?.forEachIndexed { index, eblanApplicationInfo ->
                val row = index / columns
                val column = index % columns

                subcompose(
                    eblanApplicationInfo.serialNumber to
                        eblanApplicationInfo.componentName,
                ) {
                    Box(
                        modifier = Modifier.gridItem(
                            width = cellWidth,
                            height = cellHeight,
                            x = column * cellWidth,
                            y = row * cellHeight,
                        ),
                    ) {
                        content(eblanApplicationInfo)
                    }
                }.forEach { measurable ->
                    measurable.measure(
                        Constraints.fixed(
                            width = cellWidth,
                            height = cellHeight,
                        ),
                    ).placeRelative(
                        x = column * cellWidth,
                        y = row * cellHeight,
                    )
                }
            }
        }
    }
}

@Composable
internal fun animateGridIntAsState(
    targetValue: Int,
    animate: Boolean,
): Int {
    val animatable = remember {
        Animatable(
            initialValue = targetValue,
            typeConverter = Int.VectorConverter,
        )
    }

    LaunchedEffect(key1 = targetValue, key2 = animate) {
        if (animate) {
            animatable.animateTo(
                targetValue = targetValue,
                animationSpec = spring(visibilityThreshold = Int.VisibilityThreshold),
            )
        } else {
            animatable.snapTo(targetValue)
        }
    }

    return if (animate) animatable.value else targetValue
}

@Composable
private fun GridLayoutItem(
    // Item and Dimensions
    gridItem: GridItem,
    cellWidth: Int,
    cellHeight: Int,
    // Behavior and Modifier
    animate: Boolean,
    modifier: Modifier = Modifier,
    // Content
    content: @Composable (BoxScope.(GridItem) -> Unit),
) {
    val width = gridItem.columnSpan * cellWidth
    val height = gridItem.rowSpan * cellHeight

    val x = gridItem.startColumn * cellWidth
    val y = gridItem.startRow * cellHeight

    val animatedWidth = animateGridIntAsState(targetValue = width, animate = animate)
    val animatedHeight = animateGridIntAsState(targetValue = height, animate = animate)
    val animatedX = animateGridIntAsState(targetValue = x, animate = animate)
    val animatedY = animateGridIntAsState(targetValue = y, animate = animate)

    Box(
        modifier = modifier.gridItem(
            width = animatedWidth,
            height = animatedHeight,
            x = animatedX,
            y = animatedY,
        ),
        content = {
            content(gridItem)
        },
    )
}

private data class GridItemParentData(
    val width: Int,
    val height: Int,
    val x: Int,
    val y: Int,
)

private fun Modifier.gridItem(
    width: Int,
    height: Int,
    x: Int,
    y: Int,
): Modifier = then(
    object : ParentDataModifier {
        override fun Density.modifyParentData(parentData: Any?): Any = GridItemParentData(
            width = width,
            height = height,
            x = x,
            y = y,
        )
    },
)

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
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ParentDataModifier
import androidx.compose.ui.layout.SubcomposeLayout
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Density
import com.eblan.launcher.domain.model.EblanApplicationInfo
import com.eblan.launcher.domain.model.GridItem

@Composable
internal fun GridLayout(
    modifier: Modifier = Modifier,
    columns: Int,
    gridItems: List<GridItem>?,
    rows: Int,
    animate: Boolean,
    content: @Composable BoxScope.(GridItem) -> Unit,
) {
    SubcomposeLayout(modifier = modifier) { constraints ->
        val cellWidth = constraints.maxWidth / columns

        val cellHeight = constraints.maxHeight / rows

        layout(width = constraints.maxWidth, height = constraints.maxHeight) {
            gridItems?.forEach { gridItem ->
                subcompose(gridItem.id) {
                    GridLayoutItem(
                        gridItem = gridItem,
                        cellWidth = cellWidth,
                        cellHeight = cellHeight,
                        animate = animate,
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
internal fun HorizontalAppDrawerGridLayout(
    modifier: Modifier = Modifier,
    columns: Int,
    eblanApplicationInfoWithIconPackInfos: List<EblanApplicationInfo>?,
    rows: Int,
    content: @Composable BoxScope.(EblanApplicationInfo) -> Unit,
) {
    SubcomposeLayout(modifier = modifier) { constraints ->
        val cellWidth = constraints.maxWidth / columns

        val cellHeight = constraints.maxHeight / rows

        layout(constraints.maxWidth, constraints.maxHeight) {
            eblanApplicationInfoWithIconPackInfos?.forEachIndexed { index, eblanApplicationInfoWithIconPackInfo ->
                val row = index / columns

                val column = index % columns

                subcompose(
                    eblanApplicationInfoWithIconPackInfo.serialNumber to
                        eblanApplicationInfoWithIconPackInfo.componentName,
                ) {
                    Box(
                        modifier = Modifier.gridItem(
                            width = cellWidth,
                            height = cellHeight,
                            x = column * cellWidth,
                            y = row * cellHeight,
                        ),
                    ) {
                        content(eblanApplicationInfoWithIconPackInfo)
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

    LaunchedEffect(
        key1 = targetValue,
        key2 = animate,
    ) {
        if (animate) {
            animatable.animateTo(
                targetValue = targetValue,
                animationSpec = spring(visibilityThreshold = Int.VisibilityThreshold),
            )
        } else {
            animatable.snapTo(targetValue)
        }
    }

    return animatable.value
}

@Composable
private fun GridLayoutItem(
    modifier: Modifier = Modifier,
    gridItem: GridItem,
    cellWidth: Int,
    cellHeight: Int,
    animate: Boolean,
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

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

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ParentDataModifier
import androidx.compose.ui.layout.SubcomposeLayout
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Density
import com.eblan.launcher.domain.model.GridItem
import com.eblan.launcher.domain.usecase.grid.FOLDER_PREVIEW_COLUMNS
import com.eblan.launcher.domain.usecase.grid.FOLDER_PREVIEW_ROWS

@Composable
internal fun <T> PreviewFolderGridLayout(
    modifier: Modifier = Modifier,
    gridItems: List<T>?,
    previewColumns: Int = FOLDER_PREVIEW_COLUMNS,
    previewRows: Int = FOLDER_PREVIEW_ROWS,
    slotId: (T) -> String,
    content: @Composable BoxScope.(T) -> Unit,
) {
    SubcomposeLayout(modifier = modifier) { constraints ->
        val previewCellSize = minOf(
            constraints.maxWidth,
            constraints.maxHeight,
        ) / maxOf(
            previewColumns,
            previewRows,
        )

        val previewGridWidth = previewCellSize * previewColumns

        val previewGridHeight = previewCellSize * previewRows

        val previewOffsetX = (constraints.maxWidth - previewGridWidth) / 2

        val previewOffsetY = (constraints.maxHeight - previewGridHeight) / 2

        layout(
            width = constraints.maxWidth,
            height = constraints.maxHeight,
        ) {
            gridItems?.forEachIndexed { index, gridItem ->
                subcompose(slotId(gridItem)) {
                    val x = previewOffsetX + (index % previewColumns) * previewCellSize

                    val y = previewOffsetY + (index / previewColumns) * previewCellSize

                    Box(
                        modifier = Modifier.folderGridItem(
                            x = x,
                            y = y,
                            width = previewCellSize,
                            height = previewCellSize,
                        ),
                    ) {
                        content(gridItem)
                    }
                }.forEach { measurable ->
                    val parentData = measurable.parentData as FolderGridItemParentData

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
internal fun FolderGridLayout(
    modifier: Modifier = Modifier,
    gridItems: List<GridItem>?,
    columns: Int,
    rows: Int,
    width: Int,
    height: Int,
    animate: Boolean,
    content: @Composable BoxScope.(GridItem) -> Unit,
) {
    SubcomposeLayout(modifier = modifier) { constraints ->
        val cellWidth = width / columns
        val cellHeight = height / rows

        layout(
            width = constraints.maxWidth,
            height = constraints.maxHeight,
        ) {
            gridItems?.forEachIndexed { index, gridItem ->
                subcompose(gridItem.id) {
                    FolderGridLayoutContent(
                        index = index,
                        columns = columns,
                        cellWidth = cellWidth,
                        cellHeight = cellHeight,
                        gridItem = gridItem,
                        animate = animate,
                        content = content,
                    )
                }.forEach { measurable ->
                    val parentData = measurable.parentData as FolderGridItemParentData

                    measurable.measure(
                        Constraints.fixed(
                            width = parentData.width,
                            height = parentData.height,
                        ),
                    ).placeRelativeWithLayer(
                        x = parentData.x,
                        y = parentData.y,
                    )
                }
            }
        }
    }
}

@Composable
private fun FolderGridLayoutContent(
    modifier: Modifier = Modifier,
    index: Int,
    columns: Int,
    cellWidth: Int,
    cellHeight: Int,
    gridItem: GridItem,
    animate: Boolean,
    content: @Composable (BoxScope.(GridItem) -> Unit),
) {
    val x = (index % columns) * cellWidth
    val y = (index / columns) * cellHeight

    val animatedX = animateGridIntAsState(targetValue = x, animate = animate)
    val animatedY = animateGridIntAsState(targetValue = y, animate = animate)

    Box(
        modifier = modifier.folderGridItem(
            x = animatedX,
            y = animatedY,
            width = cellWidth,
            height = cellHeight,
        ),
    ) {
        content(gridItem)
    }
}

private fun Modifier.folderGridItem(
    x: Int,
    y: Int,
    width: Int,
    height: Int,
) = then(
    object : ParentDataModifier {
        override fun Density.modifyParentData(parentData: Any?) = FolderGridItemParentData(
            x = x,
            y = y,
            width = width,
            height = height,
        )
    },
)

private data class FolderGridItemParentData(
    val x: Int,
    val y: Int,
    val width: Int,
    val height: Int,
)

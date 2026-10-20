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
package com.eblan.launcher.domain.grid

import com.eblan.launcher.domain.model.grid.Anchor
import com.eblan.launcher.domain.model.grid.GridItem
import com.eblan.launcher.domain.model.grid.SideAnchor

/**
 * Resizes a grid item to pixel dimensions while keeping the selected corner fixed.
 *
 * Pixel dimensions are rounded up to whole grid cells, with each span limited to at least one cell.
 * The resulting position is not clamped to the grid bounds.
 *
 * @param gridItem The item to resize.
 * @param width The requested width in pixels.
 * @param height The requested height in pixels.
 * @param rows The number of rows in the grid.
 * @param columns The number of columns in the grid.
 * @param gridWidth The grid width in pixels.
 * @param gridHeight The grid height in pixels.
 * @param anchor The corner to keep fixed during resizing.
 * @return A copy of [gridItem] with updated position and span.
 */
fun resizeGridItemWithPixels(
    gridItem: GridItem,
    width: Int,
    height: Int,
    rows: Int,
    columns: Int,
    gridWidth: Int,
    gridHeight: Int,
    anchor: Anchor,
): GridItem {
    val cellWidth = gridWidth / columns

    val cellHeight = gridHeight / rows

    val (newColumnSpan, newRowSpan) = pixelDimensionsToGridSpan(
        width = width,
        height = height,
        gridCellWidth = cellWidth,
        gridCellHeight = cellHeight,
    )

    val (newStartColumn, newStartRow) = resizeGridItemByAnchor(
        gridItem = gridItem,
        columnSpan = newColumnSpan,
        rowSpan = newRowSpan,
        anchor = anchor,
    )

    return gridItem.copy(
        startColumn = newStartColumn,
        startRow = newStartRow,
        columnSpan = newColumnSpan,
        rowSpan = newRowSpan,
    )
}

/**
 * Resizes a widget grid item to pixel dimensions while keeping the selected side fixed.
 *
 * Pixel dimensions are rounded up to whole grid cells, with each span limited to at least one cell.
 * The resulting position is not clamped to the grid bounds.
 *
 * @param gridItem The widget item to resize.
 * @param width The requested width in pixels.
 * @param height The requested height in pixels.
 * @param rows The number of rows in the grid.
 * @param columns The number of columns in the grid.
 * @param gridWidth The grid width in pixels.
 * @param gridHeight The grid height in pixels.
 * @param anchor The side to keep fixed during resizing.
 * @return A copy of [gridItem] with updated position and span.
 */
fun resizeWidgetGridItemWithPixels(
    gridItem: GridItem,
    width: Int,
    height: Int,
    rows: Int,
    columns: Int,
    gridWidth: Int,
    gridHeight: Int,
    anchor: SideAnchor,
): GridItem {
    val cellWidth = gridWidth / columns

    val cellHeight = gridHeight / rows

    val (newColumnSpan, newRowSpan) = pixelDimensionsToGridSpan(
        width = width,
        height = height,
        gridCellWidth = cellWidth,
        gridCellHeight = cellHeight,
    )

    val (newStartColumn, newStartRow) = resizeGridItemBySideAnchor(
        gridItem = gridItem,
        columnSpan = newColumnSpan,
        rowSpan = newRowSpan,
        anchor = anchor,
    )

    return gridItem.copy(
        startColumn = newStartColumn,
        startRow = newStartRow,
        columnSpan = newColumnSpan,
        rowSpan = newRowSpan,
    )
}

/**
 * Converts pixel dimensions to grid-cell spans, rounding up and enforcing a minimum span of one.
 *
 * @param width The width in pixels.
 * @param height The height in pixels.
 * @param gridCellWidth The width of one grid cell in pixels.
 * @param gridCellHeight The height of one grid cell in pixels.
 * @return The span as a pair of column count followed by row count.
 */
private fun pixelDimensionsToGridSpan(
    width: Int,
    height: Int,
    gridCellWidth: Int,
    gridCellHeight: Int,
): Pair<Int, Int> {
    val columnSpan = ((width + gridCellWidth - 1) / gridCellWidth).coerceAtLeast(1)

    val rowSpan = ((height + gridCellHeight - 1) / gridCellHeight).coerceAtLeast(1)

    return columnSpan to rowSpan
}

/**
 * Calculates a resized item's start position so the selected corner remains fixed.
 *
 * @param gridItem The item before resizing.
 * @param columnSpan The resized width in grid cells.
 * @param rowSpan The resized height in grid cells.
 * @param anchor The corner to keep fixed.
 * @return The new start position as a pair of column followed by row.
 */
private fun resizeGridItemByAnchor(
    gridItem: GridItem,
    columnSpan: Int,
    rowSpan: Int,
    anchor: Anchor,
): Pair<Int, Int> {
    val newStartColumn: Int

    val newStartRow: Int

    when (anchor) {
        Anchor.TopStart -> {
            newStartColumn = gridItem.startColumn

            newStartRow = gridItem.startRow
        }

        Anchor.TopEnd -> {
            newStartColumn = gridItem.startColumn + gridItem.columnSpan - columnSpan

            newStartRow = gridItem.startRow
        }

        Anchor.BottomStart -> {
            newStartColumn = gridItem.startColumn

            newStartRow = gridItem.startRow + gridItem.rowSpan - rowSpan
        }

        Anchor.BottomEnd -> {
            newStartColumn = gridItem.startColumn + gridItem.columnSpan - columnSpan

            newStartRow = gridItem.startRow + gridItem.rowSpan - rowSpan
        }
    }

    return newStartColumn to newStartRow
}

/**
 * Calculates a resized widget's start position so the selected side remains fixed.
 *
 * The item's start position on the unanchored axis remains unchanged.
 *
 * @param gridItem The widget item before resizing.
 * @param columnSpan The resized width in grid cells.
 * @param rowSpan The resized height in grid cells.
 * @param anchor The side to keep fixed.
 * @return The new start position as a pair of column followed by row.
 */
private fun resizeGridItemBySideAnchor(
    gridItem: GridItem,
    columnSpan: Int,
    rowSpan: Int,
    anchor: SideAnchor,
): Pair<Int, Int> {
    val newStartColumn: Int

    val newStartRow: Int

    when (anchor) {
        SideAnchor.Top -> {
            newStartColumn = gridItem.startColumn

            newStartRow = gridItem.startRow
        }

        SideAnchor.Bottom -> {
            newStartColumn = gridItem.startColumn

            newStartRow = gridItem.startRow + gridItem.rowSpan - rowSpan
        }

        SideAnchor.Left -> {
            newStartColumn = gridItem.startColumn

            newStartRow = gridItem.startRow
        }

        SideAnchor.Right -> {
            newStartColumn = gridItem.startColumn + gridItem.columnSpan - columnSpan

            newStartRow = gridItem.startRow
        }
    }

    return newStartColumn to newStartRow
}

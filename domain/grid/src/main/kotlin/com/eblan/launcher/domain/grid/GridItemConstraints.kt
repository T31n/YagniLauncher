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

import com.eblan.launcher.domain.model.grid.GridItem
import com.eblan.launcher.domain.model.grid.GridItemSection
import com.eblan.launcher.domain.model.grid.ResolveDirection
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive

/**
 * Checks whether a grid item's full span fits within the grid.
 *
 * @param gridItem The item whose position and span are checked.
 * @param columns The number of columns in the grid.
 * @param rows The number of rows in the grid.
 * @return `true` if the item's start and end positions are within the grid.
 */
fun isGridItemSpanWithinBounds(
    gridItem: GridItem,
    columns: Int,
    rows: Int,
): Boolean = gridItem.startColumn in 0 until columns &&
    gridItem.startRow in 0 until rows &&
    gridItem.startColumn + gridItem.columnSpan <= columns &&
    gridItem.startRow + gridItem.rowSpan <= rows

/**
 * Checks whether two grid items occupy at least one common cell.
 *
 * Items whose edges only touch do not overlap.
 *
 * @param moving The first item to compare.
 * @param other The second item to compare.
 * @return `true` if the items' occupied cells overlap.
 */
fun rectanglesOverlap(
    moving: GridItem,
    other: GridItem,
): Boolean {
    val movingLeft = moving.startColumn
    val movingRight = moving.startColumn + moving.columnSpan
    val movingTop = moving.startRow
    val movingBottom = moving.startRow + moving.rowSpan

    val otherLeft = other.startColumn
    val otherRight = other.startColumn + other.columnSpan
    val otherTop = other.startRow
    val otherBottom = other.startRow + other.rowSpan

    return movingRight > otherLeft &&
        movingLeft < otherRight &&
        movingBottom > otherTop &&
        movingTop < otherBottom
}

/**
 * Chooses a conflict-resolution direction from a horizontal coordinate within the grid.
 *
 * The item's left, middle, and right thirds map to [GridItemSection.Right],
 * [GridItemSection.Center], and [GridItemSection.Left], respectively.
 *
 * @param gridItem The item whose horizontal span is divided into thirds.
 * @param x The horizontal pixel coordinate, measured from the grid's left edge.
 * @param columns The number of columns in the grid.
 * @param gridWidth The grid width in pixels.
 * @return The direction selected for resolving a conflict.
 */
fun getGridItemSection(
    gridItem: GridItem,
    x: Int,
    columns: Int,
    gridWidth: Int,
): GridItemSection {
    val cellWidth = gridWidth / columns

    val gridItemX = gridItem.startColumn * cellWidth

    val gridItemWidth = gridItem.columnSpan * cellWidth

    val xInGridItem = x - gridItemX

    return when {
        xInGridItem < gridItemWidth / 3 -> GridItemSection.Right
        xInGridItem < 2 * gridItemWidth / 3 -> GridItemSection.Center
        else -> GridItemSection.Left
    }
}

/**
 * Finds the grid item occupying the cell at the given pixel coordinates, excluding one item ID.
 *
 * @param id The ID of the item to exclude from the search.
 * @param gridItems The items to search.
 * @param columns The number of columns in the grid.
 * @param rows The number of rows in the grid.
 * @param x The horizontal pixel coordinate, measured from the grid's left edge.
 * @param y The vertical pixel coordinate, measured from the grid's top edge.
 * @param gridWidth The grid width in pixels.
 * @param gridHeight The grid height in pixels.
 * @return The first matching item, or `null` if no other item occupies that cell.
 */
fun getGridItemByCoordinates(
    id: String,
    gridItems: List<GridItem>,
    columns: Int,
    rows: Int,
    x: Int,
    y: Int,
    gridWidth: Int,
    gridHeight: Int,
): GridItem? {
    val cellWidth = gridWidth / columns
    val cellHeight = gridHeight / rows

    return gridItems.find { gridItem ->
        val startColumn = x / cellWidth
        val startRow = y / cellHeight

        val columnInSpan =
            startColumn in gridItem.startColumn until (gridItem.startColumn + gridItem.columnSpan)

        val rowInSpan = startRow in gridItem.startRow until (gridItem.startRow + gridItem.rowSpan)

        gridItem.id != id && rowInSpan && columnInSpan
    }
}

/**
 * Determines which direction to use when resolving an overlap based on the items' positions.
 *
 * Horizontal position takes precedence; when both items start in the same column, their row
 * positions determine the direction.
 *
 * @param moving The item being moved.
 * @param other The item it overlaps.
 * @return The relative resolution direction, or `null` if both items have the same start position.
 */
fun getRelativeResolveDirection(
    moving: GridItem,
    other: GridItem,
): ResolveDirection? = when {
    moving.startColumn < other.startColumn -> ResolveDirection.Right
    moving.startColumn > other.startColumn -> ResolveDirection.Left
    moving.startRow > other.startRow -> ResolveDirection.Left
    moving.startRow < other.startRow -> ResolveDirection.Right
    else -> null
}

/**
 * Searches pages in order for the first region that can contain [gridItem] without overlapping
 * any item in [gridItems].
 *
 * Within each page, rows and then columns are searched from their lowest indices upward.
 *
 * @param gridItems The items that already occupy grid regions.
 * @param gridItem The item to place; its span and other properties are preserved.
 * @param pageCount The number of pages to search, starting at page zero.
 * @param columns The number of columns on each page.
 * @param rows The number of rows on each page.
 * @return A copy of [gridItem] with the first available page and position, or `null` if none fits.
 */
suspend fun findAvailableRegionByPage(
    gridItems: List<GridItem>,
    gridItem: GridItem,
    pageCount: Int,
    columns: Int,
    rows: Int,
): GridItem? {
    for (page in 0 until pageCount) {
        currentCoroutineContext().ensureActive()

        for (row in 0..(rows - gridItem.rowSpan)) {
            for (column in 0..(columns - gridItem.columnSpan)) {
                val candidateGridItem = gridItem.copy(
                    page = page,
                    startColumn = column,
                    startRow = row,
                )

                val isFree = gridItems.none {
                    it.page == page && rectanglesOverlap(
                        moving = candidateGridItem,
                        other = it,
                    )
                }

                if (isFree) {
                    return candidateGridItem
                }
            }
        }
    }

    return null
}

/**
 * Calculates a widget's grid span from target cell dimensions or its minimum pixel dimensions.
 *
 * A target dimension of zero uses the corresponding minimum pixel dimension, rounded up to whole
 * cells.
 *
 * @param cellWidth The width of one grid cell in pixels.
 * @param cellHeight The height of one grid cell in pixels.
 * @param minWidth The minimum widget width in pixels.
 * @param minHeight The minimum widget height in pixels.
 * @param targetCellWidth The requested width in cells, or zero to use [minWidth].
 * @param targetCellHeight The requested height in cells, or zero to use [minHeight].
 * @return The span as a pair of column count followed by row count.
 */
fun getWidgetGridItemSpan(
    cellWidth: Int,
    cellHeight: Int,
    minWidth: Int,
    minHeight: Int,
    targetCellWidth: Int,
    targetCellHeight: Int,
): Pair<Int, Int> {
    val rowSpan = if (targetCellHeight == 0) {
        (minHeight + cellHeight - 1) / cellHeight
    } else {
        targetCellHeight
    }

    val columnSpan = if (targetCellWidth == 0) {
        (minWidth + cellWidth - 1) / cellWidth
    } else {
        targetCellWidth
    }

    return columnSpan to rowSpan
}

/**
 * Calculates a widget's pixel size from its target cell dimensions or minimum pixel dimensions.
 *
 * Positive target cell dimensions are converted to pixels; non-positive values use the matching
 * minimum pixel dimension.
 *
 * @param columns The number of columns in the grid.
 * @param rows The number of rows in the grid.
 * @param gridWidth The grid width in pixels.
 * @param gridHeight The grid height in pixels.
 * @param minWidth The minimum widget width in pixels.
 * @param minHeight The minimum widget height in pixels.
 * @param targetCellWidth The requested width in cells, or a non-positive value to use [minWidth].
 * @param targetCellHeight The requested height in cells, or a non-positive value to use [minHeight].
 * @return The widget size as a pair of width followed by height in pixels.
 */
fun getWidgetGridItemSize(
    columns: Int,
    rows: Int,
    gridWidth: Int,
    gridHeight: Int,
    minWidth: Int,
    minHeight: Int,
    targetCellWidth: Int,
    targetCellHeight: Int,
): Pair<Int, Int> {
    val cellWidth = gridWidth / columns
    val cellHeight = gridHeight / rows

    val width = if (targetCellWidth > 0) {
        targetCellWidth * cellWidth
    } else {
        minWidth
    }

    val height = if (targetCellHeight > 0) {
        targetCellHeight * cellHeight
    } else {
        minHeight
    }

    return width to height
}

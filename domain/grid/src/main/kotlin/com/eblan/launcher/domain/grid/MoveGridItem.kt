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
import com.eblan.launcher.domain.model.grid.ResolveDirection
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive

/**
 * Moves overlapping items according to [resolveDirection], processing any resulting overlaps.
 *
 * This function resolves conflicts on a copy of [gridItems]. If an item cannot be moved within
 * the grid, it returns `null` without changing the original list.
 *
 * @param gridItems The items to resolve.
 * @param resolveDirection The direction used to reposition conflicting items.
 * @param movingGridItem The item whose conflicts are resolved first.
 * @param columns The number of columns in the grid.
 * @param rows The number of rows in the grid.
 * @return The resolved items, or `null` if a conflicting item cannot be moved.
 */
suspend fun resolveConflicts(
    gridItems: List<GridItem>,
    resolveDirection: ResolveDirection,
    movingGridItem: GridItem,
    columns: Int,
    rows: Int,
): List<GridItem>? {
    val resolvedGridItems = gridItems.toMutableList()
    val queue = ArrayDeque<String>()

    queue.add(movingGridItem.id)

    while (queue.isNotEmpty()) {
        currentCoroutineContext().ensureActive()

        val currentId = queue.removeFirst()

        val currentIndex = resolvedGridItems.indexOfFirst { it.id == currentId }

        if (currentIndex == -1) continue

        val current = resolvedGridItems[currentIndex]

        for (i in resolvedGridItems.indices) {
            val other = resolvedGridItems[i]

            if (other.id == current.id) continue

            if (!rectanglesOverlap(
                    moving = current,
                    other = other,
                )
            ) {
                continue
            }

            val movedGridItem = moveGridItem(
                resolveDirection = resolveDirection,
                moving = current,
                conflicting = other,
                columns = columns,
                rows = rows,
            ) ?: return null

            resolvedGridItems[i] = movedGridItem

            queue.add(movedGridItem.id)
        }
    }

    return resolvedGridItems
}

/**
 * Selects the repositioning strategy for a conflicting item.
 *
 * @param resolveDirection The direction in which to resolve the conflict.
 * @param moving The item that caused the conflict.
 * @param conflicting The item to reposition.
 * @param columns The number of columns in the grid.
 * @param rows The number of rows in the grid.
 * @return The repositioned item, or `null` if it cannot fit in the grid.
 */
private fun moveGridItem(
    resolveDirection: ResolveDirection,
    moving: GridItem,
    conflicting: GridItem,
    columns: Int,
    rows: Int,
): GridItem? = when (resolveDirection) {
    ResolveDirection.Left -> {
        moveGridItemToLeft(
            moving = moving,
            conflicting = conflicting,
            columns = columns,
            rows = rows,
        )
    }

    ResolveDirection.Right -> {
        moveGridItemToRight(
            moving = moving,
            conflicting = conflicting,
            columns = columns,
            rows = rows,
        )
    }

    ResolveDirection.Center -> moving
}

/**
 * Places the conflicting item immediately to the right of [moving], wrapping to the next row
 * beneath it when there is not enough horizontal space.
 *
 * @param moving The item that caused the conflict.
 * @param conflicting The item to reposition.
 * @param columns The number of columns in the grid.
 * @param rows The number of rows in the grid.
 * @return The repositioned item, or `null` if its resulting row exceeds the grid.
 */
private fun moveGridItemToRight(
    moving: GridItem,
    conflicting: GridItem,
    columns: Int,
    rows: Int,
): GridItem? {
    var newStartColumn = moving.startColumn + moving.columnSpan
    var newStartRow = conflicting.startRow

    if (newStartColumn + conflicting.columnSpan > columns) {
        newStartColumn = 0
        newStartRow = moving.startRow + moving.rowSpan
    }

    return if (newStartRow + conflicting.rowSpan <= rows) {
        conflicting.copy(
            startColumn = newStartColumn,
            startRow = newStartRow,
        )
    } else {
        null
    }
}

/**
 * Places the conflicting item immediately to the left of [moving], wrapping to the last column
 * on the row above it when there is not enough horizontal space.
 *
 * @param moving The item that caused the conflict.
 * @param conflicting The item to reposition.
 * @param columns The number of columns in the grid.
 * @param rows The number of rows in the grid.
 * @return The repositioned item, or `null` if its resulting row is outside the grid.
 */
private fun moveGridItemToLeft(
    moving: GridItem,
    conflicting: GridItem,
    columns: Int,
    rows: Int,
): GridItem? {
    var newStartColumn = moving.startColumn - conflicting.columnSpan
    var newStartRow = conflicting.startRow

    if (newStartColumn < 0) {
        newStartColumn = columns - conflicting.columnSpan
        newStartRow = moving.startRow - 1
    }

    return if (
        newStartRow >= 0 &&
        newStartRow + conflicting.rowSpan <= rows
    ) {
        conflicting.copy(
            startColumn = newStartColumn,
            startRow = newStartRow,
        )
    } else {
        null
    }
}

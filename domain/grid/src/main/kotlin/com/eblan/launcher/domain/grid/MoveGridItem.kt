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

suspend fun resolveConflicts(
    gridItems: MutableList<GridItem>,
    resolveDirection: ResolveDirection,
    movingGridItem: GridItem,
    columns: Int,
    rows: Int,
): Boolean {
    val queue = ArrayDeque<String>()

    queue.add(movingGridItem.id)

    while (queue.isNotEmpty()) {
        currentCoroutineContext().ensureActive()

        val currentId = queue.removeFirst()

        val currentIndex = gridItems.indexOfFirst { it.id == currentId }

        if (currentIndex == -1) continue

        val current = gridItems[currentIndex]

        for (i in gridItems.indices) {
            val other = gridItems[i]

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
            ) ?: return false

            gridItems[i] = movedGridItem

            queue.add(movedGridItem.id)
        }
    }

    return true
}

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

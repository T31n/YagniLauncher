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

import com.eblan.launcher.domain.model.getEblanAction
import com.eblan.launcher.domain.model.getGridItemSettings
import com.eblan.launcher.domain.model.grid.Associate
import com.eblan.launcher.domain.model.grid.GridItem
import com.eblan.launcher.domain.model.grid.GridItemData
import com.eblan.launcher.domain.model.grid.ResolveDirection
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class MoveGridItemTest {
    @Test
    fun `returns true when there are no conflicts`() {
        val moving = getGridItem(
            id = "moving",
            startColumn = 0,
            startRow = 0,
            columnSpan = 1,
            rowSpan = 1,
        )

        val other = getGridItem(
            id = "other",
            startColumn = 2,
            startRow = 0,
            columnSpan = 1,
            rowSpan = 1,
        )

        val gridItems = mutableListOf(moving, other)

        val result = runBlocking {
            resolveConflicts(
                gridItems = gridItems,
                resolveDirection = ResolveDirection.Right,
                movingGridItem = moving,
                columns = 5,
                rows = 4,
            )
        }

        assertTrue(result)
        assertEquals(
            listOf(moving, other),
            gridItems,
        )
    }

    @Test
    fun `moves conflicting item to the right`() {
        val moving = getGridItem(
            id = "moving",
            startColumn = 0,
            startRow = 0,
            columnSpan = 1,
            rowSpan = 1,
        )

        val conflicting = getGridItem(
            id = "conflicting",
            startColumn = 0,
            startRow = 0,
            columnSpan = 1,
            rowSpan = 1,
        )

        val gridItems = mutableListOf(moving, conflicting)

        val result = runBlocking {
            resolveConflicts(
                gridItems = gridItems,
                resolveDirection = ResolveDirection.Right,
                movingGridItem = moving,
                columns = 5,
                rows = 4,
            )
        }

        assertTrue(result)

        assertEquals(
            conflicting.copy(
                startColumn = 1,
                startRow = 0,
            ),
            gridItems[1],
        )
    }

    @Test
    fun `moves conflicting item to the left`() {
        val moving = getGridItem(
            id = "moving",
            startColumn = 2,
            startRow = 0,
            columnSpan = 1,
            rowSpan = 1,
        )

        val conflicting = getGridItem(
            id = "conflicting",
            startColumn = 2,
            startRow = 0,
            columnSpan = 1,
            rowSpan = 1,
        )

        val gridItems = mutableListOf(moving, conflicting)

        val result = runBlocking {
            resolveConflicts(
                gridItems = gridItems,
                resolveDirection = ResolveDirection.Left,
                movingGridItem = moving,
                columns = 5,
                rows = 4,
            )
        }

        assertTrue(result)

        assertEquals(
            conflicting.copy(
                startColumn = 1,
                startRow = 0,
            ),
            gridItems[1],
        )
    }

    @Test
    fun `returns false when conflicting item cannot move right`() {
        val moving = getGridItem(
            id = "moving",
            startColumn = 4,
            startRow = 3,
            columnSpan = 1,
            rowSpan = 1,
        )

        val conflicting = getGridItem(
            id = "conflicting",
            startColumn = 4,
            startRow = 3,
            columnSpan = 1,
            rowSpan = 1,
        )

        val gridItems = mutableListOf(moving, conflicting)

        val result = runBlocking {
            resolveConflicts(
                gridItems = gridItems,
                resolveDirection = ResolveDirection.Right,
                movingGridItem = moving,
                columns = 5,
                rows = 4,
            )
        }

        assertFalse(result)
    }

    @Test
    fun `returns false when conflicting item cannot move left`() {
        val moving = getGridItem(
            id = "moving",
            startColumn = 0,
            startRow = 0,
            columnSpan = 1,
            rowSpan = 1,
        )

        val conflicting = getGridItem(
            id = "conflicting",
            startColumn = 0,
            startRow = 0,
            columnSpan = 1,
            rowSpan = 1,
        )

        val gridItems = mutableListOf(moving, conflicting)

        val result = runBlocking {
            resolveConflicts(
                gridItems = gridItems,
                resolveDirection = ResolveDirection.Left,
                movingGridItem = moving,
                columns = 5,
                rows = 4,
            )
        }

        assertFalse(result)
    }

    @Test
    fun `moves conflicting item to next row when moving right exceeds column bounds`() {
        val moving = getGridItem(
            id = "moving",
            startColumn = 4,
            startRow = 0,
            columnSpan = 1,
            rowSpan = 1,
        )

        val conflicting = getGridItem(
            id = "conflicting",
            startColumn = 4,
            startRow = 0,
            columnSpan = 1,
            rowSpan = 1,
        )

        val gridItems = mutableListOf(moving, conflicting)

        val result = runBlocking {
            resolveConflicts(
                gridItems = gridItems,
                resolveDirection = ResolveDirection.Right,
                movingGridItem = moving,
                columns = 5,
                rows = 4,
            )
        }

        assertTrue(result)

        assertEquals(
            conflicting.copy(
                startColumn = 0,
                startRow = 1,
            ),
            gridItems[1],
        )
    }

    @Test
    fun `moves conflicting item to previous row when moving left exceeds column bounds`() {
        val moving = getGridItem(
            id = "moving",
            startColumn = 0,
            startRow = 1,
            columnSpan = 1,
            rowSpan = 1,
        )

        val conflicting = getGridItem(
            id = "conflicting",
            startColumn = 0,
            startRow = 1,
            columnSpan = 1,
            rowSpan = 1,
        )

        val gridItems = mutableListOf(moving, conflicting)

        val result = runBlocking {
            resolveConflicts(
                gridItems = gridItems,
                resolveDirection = ResolveDirection.Left,
                movingGridItem = moving,
                columns = 5,
                rows = 4,
            )
        }

        assertTrue(result)

        assertEquals(
            conflicting.copy(
                startColumn = 4,
                startRow = 0,
            ),
            gridItems[1],
        )
    }

    private fun getGridItem(
        id: String = "Test",
        page: Int = 0,
        startColumn: Int,
        startRow: Int,
        columnSpan: Int,
        rowSpan: Int,
    ) = GridItem(
        id = id,
        page = page,
        startColumn = startColumn,
        startRow = startRow,
        columnSpan = columnSpan,
        rowSpan = rowSpan,
        data = GridItemData.Folder(
            label = "Test",
            icon = null,
            index = 0,
            folderId = null,
        ),
        associate = Associate.Grid,
        override = false,
        gridItemSettings = getGridItemSettings(),
        doubleTap = getEblanAction(),
        swipeUp = getEblanAction(),
        swipeDown = getEblanAction(),
    )
}

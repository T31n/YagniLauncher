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
package com.eblan.launcher.domain.usecase.grid

import com.eblan.launcher.domain.model.getEblanAction
import com.eblan.launcher.domain.model.getGridItemSettings
import com.eblan.launcher.domain.model.grid.ApplicationInfoGridItem
import com.eblan.launcher.domain.model.grid.Associate
import com.eblan.launcher.domain.model.grid.GridItems
import com.eblan.launcher.domain.model.grid.WidgetGridItem
import com.eblan.launcher.domain.repository.FakeGridRepository
import com.eblan.launcher.domain.usecase.util.toGridItems
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertNull

@OptIn(ExperimentalCoroutinesApi::class)
class MoveGridItemUseCaseTest {
    private val dispatcher = StandardTestDispatcher()

    @Test
    fun `moves grid item when there are no conflicts`() = runTest(dispatcher) {
        val applicationInfoGridItem = getApplicationInfoGridItem()

        val gridItems = GridItems(
            applicationInfoGridItems = listOf(applicationInfoGridItem),
            widgetGridItems = emptyList(),
            shortcutInfoGridItems = emptyList(),
            shortcutConfigGridItems = emptyList(),
            folderGridItems = emptyList(),
        )

        val gridRepository = FakeGridRepository(
            initialGridItems = gridItems,
        )

        val moveGridItemUseCase = MoveGridItemUseCase(
            gridRepository = gridRepository,
            defaultDispatcher = dispatcher,
        )

        val movingGridItem = gridItems.toGridItems()
            .single()
            .copy(
                startColumn = 2,
                startRow = 1,
            )

        val moveGridItemResult = moveGridItemUseCase(
            movingGridItem = movingGridItem,
            x = 250,
            y = 150,
            columns = 5,
            rows = 4,
            gridWidth = 500,
            gridHeight = 400,
        )

        assertTrue(moveGridItemResult.isSuccess)
        assertEquals(movingGridItem, moveGridItemResult.movingGridItem)
        assertNull(moveGridItemResult.conflictingGridItem)

        val persistedGridItem = gridRepository
            .getGridItems()
            .applicationInfoGridItems
            .single()

        assertEquals(2, persistedGridItem.startColumn)
        assertEquals(1, persistedGridItem.startRow)
    }

    @Test
    fun `returns conflicting grid item when moving onto normal item at center`() = runTest(dispatcher) {
        val applicationInfoGridItem = getApplicationInfoGridItem(
            id = "moving",
        )

        val conflictingApplicationInfoGridItem = getApplicationInfoGridItem(
            id = "conflicting",
            startColumn = 2,
            startRow = 1,
        )

        val gridItems = GridItems(
            applicationInfoGridItems = listOf(
                applicationInfoGridItem,
                conflictingApplicationInfoGridItem,
            ),
            widgetGridItems = emptyList(),
            shortcutInfoGridItems = emptyList(),
            shortcutConfigGridItems = emptyList(),
            folderGridItems = emptyList(),
        )

        val gridRepository = FakeGridRepository(
            initialGridItems = gridItems,
        )

        val moveGridItemUseCase = MoveGridItemUseCase(
            gridRepository = gridRepository,
            defaultDispatcher = dispatcher,
        )

        val movingGridItem = gridItems.toGridItems()
            .first { it.id == applicationInfoGridItem.id }
            .copy(
                startColumn = 2,
                startRow = 1,
            )

        val moveGridItemResult = moveGridItemUseCase(
            movingGridItem = movingGridItem,
            x = 250,
            y = 150,
            columns = 5,
            rows = 4,
            gridWidth = 500,
            gridHeight = 400,
        )

        assertTrue(moveGridItemResult.isSuccess)
        assertEquals(movingGridItem, moveGridItemResult.movingGridItem)
        assertEquals(
            conflictingApplicationInfoGridItem.id,
            moveGridItemResult.conflictingGridItem?.id,
        )
    }

    @Test
    fun `ignores grid item from different page`() = runTest(dispatcher) {
        val applicationInfoGridItem = getApplicationInfoGridItem(
            id = "moving",
            page = 0,
        )

        val otherPageApplicationInfoGridItem = getApplicationInfoGridItem(
            id = "other-page",
            page = 1,
            startColumn = 2,
            startRow = 1,
        )

        val gridItems = GridItems(
            applicationInfoGridItems = listOf(
                applicationInfoGridItem,
                otherPageApplicationInfoGridItem,
            ),
            widgetGridItems = emptyList(),
            shortcutInfoGridItems = emptyList(),
            shortcutConfigGridItems = emptyList(),
            folderGridItems = emptyList(),
        )

        val gridRepository = FakeGridRepository(
            initialGridItems = gridItems,
        )

        val moveGridItemUseCase = MoveGridItemUseCase(
            gridRepository = gridRepository,
            defaultDispatcher = dispatcher,
        )

        val movingGridItem = gridItems.toGridItems()
            .first { it.id == applicationInfoGridItem.id }
            .copy(
                startColumn = 2,
                startRow = 1,
            )

        val moveGridItemResult = moveGridItemUseCase(
            movingGridItem = movingGridItem,
            x = 250,
            y = 150,
            columns = 5,
            rows = 4,
            gridWidth = 500,
            gridHeight = 400,
        )

        assertTrue(moveGridItemResult.isSuccess)
        assertNull(moveGridItemResult.conflictingGridItem)

        val persistedGridItems = gridRepository
            .getGridItems()
            .applicationInfoGridItems

        assertEquals(
            2,
            persistedGridItems
                .first { it.id == applicationInfoGridItem.id }
                .startColumn,
        )

        assertEquals(
            1,
            persistedGridItems
                .first { it.id == applicationInfoGridItem.id }
                .startRow,
        )

        assertEquals(
            2,
            persistedGridItems
                .first { it.id == otherPageApplicationInfoGridItem.id }
                .startColumn,
        )

        assertEquals(
            1,
            persistedGridItems
                .first { it.id == otherPageApplicationInfoGridItem.id }
                .startRow,
        )
    }

    @Test
    fun `ignores grid item with different associate`() = runTest(dispatcher) {
        val applicationInfoGridItem = getApplicationInfoGridItem(
            id = "moving",
            associate = Associate.Grid,
        )

        val dockApplicationInfoGridItem = getApplicationInfoGridItem(
            id = "dock",
            startColumn = 2,
            startRow = 1,
            associate = Associate.Dock,
        )

        val gridItems = GridItems(
            applicationInfoGridItems = listOf(
                applicationInfoGridItem,
                dockApplicationInfoGridItem,
            ),
            widgetGridItems = emptyList(),
            shortcutInfoGridItems = emptyList(),
            shortcutConfigGridItems = emptyList(),
            folderGridItems = emptyList(),
        )

        val gridRepository = FakeGridRepository(
            initialGridItems = gridItems,
        )

        val moveGridItemUseCase = MoveGridItemUseCase(
            gridRepository = gridRepository,
            defaultDispatcher = dispatcher,
        )

        val movingGridItem = gridItems.toGridItems()
            .first { it.id == applicationInfoGridItem.id }
            .copy(
                startColumn = 2,
                startRow = 1,
            )

        val moveGridItemResult = moveGridItemUseCase(
            movingGridItem = movingGridItem,
            x = 250,
            y = 150,
            columns = 5,
            rows = 4,
            gridWidth = 500,
            gridHeight = 400,
        )

        assertTrue(moveGridItemResult.isSuccess)
        assertNull(moveGridItemResult.conflictingGridItem)

        val persistedGridItems = gridRepository
            .getGridItems()
            .applicationInfoGridItems

        val persistedMovingGridItem = persistedGridItems
            .first { it.id == applicationInfoGridItem.id }

        val persistedDockItem = persistedGridItems
            .first { it.id == dockApplicationInfoGridItem.id }

        assertEquals(2, persistedMovingGridItem.startColumn)
        assertEquals(1, persistedMovingGridItem.startRow)

        assertEquals(2, persistedDockItem.startColumn)
        assertEquals(1, persistedDockItem.startRow)
    }

    @Test
    fun `ignores grid item inside folder`() = runTest(dispatcher) {
        val applicationInfoGridItem = getApplicationInfoGridItem(
            id = "moving",
        )

        val folderApplicationInfoGridItem = getApplicationInfoGridItem(
            id = "folder-child",
            startColumn = 2,
            startRow = 1,
            folderId = "folder",
        )

        val gridItems = GridItems(
            applicationInfoGridItems = listOf(
                applicationInfoGridItem,
                folderApplicationInfoGridItem,
            ),
            widgetGridItems = emptyList(),
            shortcutInfoGridItems = emptyList(),
            shortcutConfigGridItems = emptyList(),
            folderGridItems = emptyList(),
        )

        val gridRepository = FakeGridRepository(
            initialGridItems = gridItems,
        )

        val moveGridItemUseCase = MoveGridItemUseCase(
            gridRepository = gridRepository,
            defaultDispatcher = dispatcher,
        )

        val movingGridItem = gridItems.toGridItems()
            .first { it.id == applicationInfoGridItem.id }
            .copy(
                startColumn = 2,
                startRow = 1,
            )

        val moveGridItemResult = moveGridItemUseCase(
            movingGridItem = movingGridItem,
            x = 250,
            y = 150,
            columns = 5,
            rows = 4,
            gridWidth = 500,
            gridHeight = 400,
        )

        assertTrue(moveGridItemResult.isSuccess)
        assertNull(moveGridItemResult.conflictingGridItem)

        val persistedGridItems = gridRepository
            .getGridItems()
            .applicationInfoGridItems

        val persistedMovingGridItem = persistedGridItems
            .first { it.id == applicationInfoGridItem.id }

        val persistedFolderChild = persistedGridItems
            .first { it.id == folderApplicationInfoGridItem.id }

        assertEquals(2, persistedMovingGridItem.startColumn)
        assertEquals(1, persistedMovingGridItem.startRow)

        assertEquals(2, persistedFolderChild.startColumn)
        assertEquals(1, persistedFolderChild.startRow)
    }

    @Test
    fun `fails when moving grid item onto widget at center`() = runTest(dispatcher) {
        val applicationInfoGridItem = getApplicationInfoGridItem(
            id = "moving",
        )

        val widgetGridItem = getWidgetGridItem()

        val gridItems = GridItems(
            applicationInfoGridItems = listOf(applicationInfoGridItem),
            widgetGridItems = listOf(widgetGridItem),
            shortcutInfoGridItems = emptyList(),
            shortcutConfigGridItems = emptyList(),
            folderGridItems = emptyList(),
        )

        val gridRepository = FakeGridRepository(
            initialGridItems = gridItems,
        )

        val moveGridItemUseCase = MoveGridItemUseCase(
            gridRepository = gridRepository,
            defaultDispatcher = dispatcher,
        )

        val movingGridItem = gridItems.toGridItems()
            .first { it.id == applicationInfoGridItem.id }
            .copy(
                startColumn = 2,
                startRow = 1,
            )

        val moveGridItemResult = moveGridItemUseCase(
            movingGridItem = movingGridItem,
            x = 250,
            y = 150,
            columns = 5,
            rows = 4,
            gridWidth = 500,
            gridHeight = 400,
        )

        assertFalse(moveGridItemResult.isSuccess)
        assertEquals(movingGridItem, moveGridItemResult.movingGridItem)
        assertNull(moveGridItemResult.conflictingGridItem)

        val persistedMovingGridItem = gridRepository
            .getGridItems()
            .applicationInfoGridItems
            .single()

        assertEquals(0, persistedMovingGridItem.startColumn)
        assertEquals(0, persistedMovingGridItem.startRow)
    }

    private fun getApplicationInfoGridItem(
        id: String = "app",
        page: Int = 0,
        startColumn: Int = 0,
        startRow: Int = 0,
        associate: Associate = Associate.Grid,
        folderId: String? = null,
    ) = ApplicationInfoGridItem(
        id = id,
        page = page,
        startColumn = startColumn,
        startRow = startRow,
        columnSpan = 1,
        rowSpan = 1,
        associate = associate,
        componentName = "com.example/.MainActivity",
        packageName = "com.example",
        icon = null,
        label = "Example",
        override = false,
        serialNumber = 0L,
        customIcon = null,
        customLabel = null,
        gridItemSettings = getGridItemSettings(),
        doubleTap = getEblanAction(),
        swipeUp = getEblanAction(),
        swipeDown = getEblanAction(),
        index = 0,
        folderId = folderId,
    )

    private fun getWidgetGridItem() = WidgetGridItem(
        id = "widget",
        page = 0,
        startColumn = 2,
        startRow = 1,
        columnSpan = 1,
        rowSpan = 1,
        associate = Associate.Grid,
        appWidgetId = 1,
        packageName = "com.example",
        serialNumber = 0L,
        componentName = "com.example/.Widget",
        configure = null,
        minWidth = 100,
        minHeight = 100,
        resizeMode = 0,
        minResizeWidth = 100,
        minResizeHeight = 100,
        maxResizeWidth = 300,
        maxResizeHeight = 300,
        targetCellHeight = 1,
        targetCellWidth = 1,
        preview = null,
        label = "Example Widget",
        icon = null,
        override = false,
        gridItemSettings = getGridItemSettings(),
    )
}

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
import com.eblan.launcher.domain.repository.FakeGridRepository
import com.eblan.launcher.domain.usecase.util.toGridItems
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ResizeGridItemUseCaseTest {
    private val dispatcher = StandardTestDispatcher()

    @Test
    fun `resizes grid item when there are no conflicts`() = runTest(dispatcher) {
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

        val resizeGridItemUseCase = ResizeGridItemUseCase(
            gridRepository = gridRepository,
            defaultDispatcher = dispatcher,
        )

        val resizingGridItem = gridItems.toGridItems()
            .first { it.id == applicationInfoGridItem.id }
            .copy(
                columnSpan = 2,
                rowSpan = 2,
            )

        val resizedGridItem = resizeGridItemUseCase(
            resizingGridItem = resizingGridItem,
            columns = 5,
            rows = 4,
        )

        assertEquals(2, resizedGridItem.columnSpan)
        assertEquals(2, resizedGridItem.rowSpan)
    }

    @Test
    fun `resizes grid item and resolves conflict`() = runTest(dispatcher) {
        val applicationInfoGridItem = getApplicationInfoGridItem(
            id = "resizing",
            columnSpan = 1,
            rowSpan = 1,
        )

        val conflictingApplicationInfoGridItem = getApplicationInfoGridItem(
            id = "conflicting",
            startColumn = 1,
            startRow = 0,
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

        val resizeGridItemUseCase = ResizeGridItemUseCase(
            gridRepository = gridRepository,
            defaultDispatcher = dispatcher,
        )

        val resizingGridItem = gridItems.toGridItems()
            .first { it.id == applicationInfoGridItem.id }
            .copy(
                columnSpan = 2,
                rowSpan = 1,
            )

        val resizedGridItem = resizeGridItemUseCase(
            resizingGridItem = resizingGridItem,
            columns = 5,
            rows = 4,
        )

        assertEquals(0, resizedGridItem.startColumn)
        assertEquals(0, resizedGridItem.startRow)
        assertEquals(2, resizedGridItem.columnSpan)
        assertEquals(1, resizedGridItem.rowSpan)

        val persistedGridItem = gridRepository
            .getGridItems()
            .applicationInfoGridItems
            .first { it.id == conflictingApplicationInfoGridItem.id }

        assertEquals(2, persistedGridItem.startColumn)
        assertEquals(0, persistedGridItem.startRow)
    }

    @Test
    fun `does not resize grid item when conflict cannot be resolved`() = runTest(dispatcher) {
        val applicationInfoGridItem = getApplicationInfoGridItem(
            id = "resizing",
            columnSpan = 1,
            rowSpan = 1,
        )

        val conflictingApplicationInfoGridItems = listOf(
            getApplicationInfoGridItem(
                id = "conflicting-1",
                startColumn = 1,
                startRow = 0,
                columnSpan = 4,
                rowSpan = 1,
            ),
            getApplicationInfoGridItem(
                id = "conflicting-2",
                startColumn = 1,
                startRow = 1,
                columnSpan = 4,
                rowSpan = 1,
            ),
            getApplicationInfoGridItem(
                id = "conflicting-3",
                startColumn = 1,
                startRow = 2,
                columnSpan = 4,
                rowSpan = 1,
            ),
            getApplicationInfoGridItem(
                id = "conflicting-4",
                startColumn = 1,
                startRow = 3,
                columnSpan = 4,
                rowSpan = 1,
            ),
        )

        val gridItems = GridItems(
            applicationInfoGridItems = listOf(
                applicationInfoGridItem,
                *conflictingApplicationInfoGridItems.toTypedArray(),
            ),
            widgetGridItems = emptyList(),
            shortcutInfoGridItems = emptyList(),
            shortcutConfigGridItems = emptyList(),
            folderGridItems = emptyList(),
        )

        val gridRepository = FakeGridRepository(
            initialGridItems = gridItems,
        )

        val resizeGridItemUseCase = ResizeGridItemUseCase(
            gridRepository = gridRepository,
            defaultDispatcher = dispatcher,
        )

        val resizingGridItem = gridItems.toGridItems()
            .first { it.id == applicationInfoGridItem.id }
            .copy(
                columnSpan = 2,
                rowSpan = 1,
            )

        val resizedGridItem = resizeGridItemUseCase(
            resizingGridItem = resizingGridItem,
            columns = 5,
            rows = 4,
        )

        assertEquals(0, resizedGridItem.startColumn)
        assertEquals(0, resizedGridItem.startRow)
        assertEquals(1, resizedGridItem.columnSpan)
        assertEquals(1, resizedGridItem.rowSpan)
    }

    @Test
    fun `ignores conflicting grid item on different page`() = runTest(dispatcher) {
        val applicationInfoGridItem = getApplicationInfoGridItem(
            id = "resizing",
            page = 0,
        )

        val otherPageApplicationInfoGridItem = getApplicationInfoGridItem(
            id = "other-page",
            page = 1,
            startColumn = 1,
            startRow = 0,
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

        val resizeGridItemUseCase = ResizeGridItemUseCase(
            gridRepository = gridRepository,
            defaultDispatcher = dispatcher,
        )

        val resizingGridItem = gridItems.toGridItems()
            .first { it.id == applicationInfoGridItem.id }
            .copy(
                columnSpan = 2,
                rowSpan = 1,
            )

        val resizedGridItem = resizeGridItemUseCase(
            resizingGridItem = resizingGridItem,
            columns = 5,
            rows = 4,
        )

        assertEquals(0, resizedGridItem.startColumn)
        assertEquals(0, resizedGridItem.startRow)
        assertEquals(2, resizedGridItem.columnSpan)
        assertEquals(1, resizedGridItem.rowSpan)
    }

    @Test
    fun `ignores conflicting grid item with different associate`() = runTest(dispatcher) {
        val applicationInfoGridItem = getApplicationInfoGridItem(
            id = "resizing",
            associate = Associate.Grid,
        )

        val dockApplicationInfoGridItem = getApplicationInfoGridItem(
            id = "dock",
            associate = Associate.Dock,
            startColumn = 1,
            startRow = 0,
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

        val resizeGridItemUseCase = ResizeGridItemUseCase(
            gridRepository = gridRepository,
            defaultDispatcher = dispatcher,
        )

        val resizingGridItem = gridItems.toGridItems()
            .first { it.id == applicationInfoGridItem.id }
            .copy(
                columnSpan = 2,
                rowSpan = 1,
            )

        val resizedGridItem = resizeGridItemUseCase(
            resizingGridItem = resizingGridItem,
            columns = 5,
            rows = 4,
        )

        assertEquals(0, resizedGridItem.startColumn)
        assertEquals(0, resizedGridItem.startRow)
        assertEquals(2, resizedGridItem.columnSpan)
        assertEquals(1, resizedGridItem.rowSpan)
    }

    @Test
    fun `ignores conflicting non top level grid item`() = runTest(dispatcher) {
        val applicationInfoGridItem = getApplicationInfoGridItem(
            id = "resizing",
        )

        val folderApplicationInfoGridItem = getApplicationInfoGridItem(
            id = "folder-item",
            startColumn = 1,
            startRow = 0,
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

        val resizeGridItemUseCase = ResizeGridItemUseCase(
            gridRepository = gridRepository,
            defaultDispatcher = dispatcher,
        )

        val resizingGridItem = gridItems.toGridItems()
            .first { it.id == applicationInfoGridItem.id }
            .copy(
                columnSpan = 2,
                rowSpan = 1,
            )

        val resizedGridItem = resizeGridItemUseCase(
            resizingGridItem = resizingGridItem,
            columns = 5,
            rows = 4,
        )

        assertEquals(0, resizedGridItem.startColumn)
        assertEquals(0, resizedGridItem.startRow)
        assertEquals(2, resizedGridItem.columnSpan)
        assertEquals(1, resizedGridItem.rowSpan)
    }

    private fun getApplicationInfoGridItem(
        id: String = "app",
        page: Int = 0,
        startColumn: Int = 0,
        startRow: Int = 0,
        columnSpan: Int = 1,
        rowSpan: Int = 1,
        associate: Associate = Associate.Grid,
        folderId: String? = null,
    ) = ApplicationInfoGridItem(
        id = id,
        page = page,
        startColumn = startColumn,
        startRow = startRow,
        columnSpan = columnSpan,
        rowSpan = rowSpan,
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
}

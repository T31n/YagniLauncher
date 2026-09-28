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
import com.eblan.launcher.domain.model.grid.SideAnchor
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class ResizeGridItemTest {
    @Test
    fun `converts pixel dimensions to grid spans`() {
        val gridItem = getGridItem(
            startColumn = 1,
            startRow = 1,
            columnSpan = 2,
            rowSpan = 2,
        )

        val result = resizeWidgetGridItemWithPixels(
            gridItem = gridItem,
            width = 250,
            height = 150,
            rows = 4,
            columns = 5,
            gridWidth = 500,
            gridHeight = 400,
            anchor = SideAnchor.Top,
        )

        assertEquals(
            gridItem.copy(
                columnSpan = 3,
                rowSpan = 2,
            ),
            result,
        )
    }

    @Test
    fun `rounds pixel dimensions up to next grid cell`() {
        val gridItem = getGridItem(
            startColumn = 1,
            startRow = 1,
            columnSpan = 1,
            rowSpan = 1,
        )

        val result = resizeWidgetGridItemWithPixels(
            gridItem = gridItem,
            width = 101,
            height = 101,
            rows = 4,
            columns = 5,
            gridWidth = 500,
            gridHeight = 400,
            anchor = SideAnchor.Top,
        )

        assertEquals(2, result.columnSpan)
        assertEquals(2, result.rowSpan)
    }

    @Test
    fun `clamps zero pixel dimensions to one cell`() {
        val gridItem = getGridItem(
            startColumn = 1,
            startRow = 1,
            columnSpan = 2,
            rowSpan = 2,
        )

        val result = resizeWidgetGridItemWithPixels(
            gridItem = gridItem,
            width = 0,
            height = 0,
            rows = 4,
            columns = 5,
            gridWidth = 500,
            gridHeight = 400,
            anchor = SideAnchor.Top,
        )

        assertEquals(1, result.columnSpan)
        assertEquals(1, result.rowSpan)
    }

    @Test
    fun `keeps top anchor fixed while resizing`() {
        val gridItem = getGridItem(
            startColumn = 2,
            startRow = 1,
            columnSpan = 2,
            rowSpan = 2,
        )

        val result = resizeWidgetGridItemWithPixels(
            gridItem = gridItem,
            width = 300,
            height = 300,
            rows = 4,
            columns = 5,
            gridWidth = 500,
            gridHeight = 400,
            anchor = SideAnchor.Top,
        )

        assertEquals(2, result.startColumn)
        assertEquals(1, result.startRow)
    }

    @Test
    fun `keeps bottom anchor fixed while resizing`() {
        val gridItem = getGridItem(
            startColumn = 2,
            startRow = 1,
            columnSpan = 2,
            rowSpan = 2,
        )

        val result = resizeWidgetGridItemWithPixels(
            gridItem = gridItem,
            width = 100,
            height = 100,
            rows = 4,
            columns = 5,
            gridWidth = 500,
            gridHeight = 400,
            anchor = SideAnchor.Bottom,
        )

        assertEquals(2, result.startColumn)
        assertEquals(2, result.startRow)
        assertEquals(1, result.columnSpan)
        assertEquals(1, result.rowSpan)
    }

    @Test
    fun `keeps left anchor fixed while resizing`() {
        val gridItem = getGridItem(
            startColumn = 2,
            startRow = 1,
            columnSpan = 2,
            rowSpan = 2,
        )

        val result = resizeWidgetGridItemWithPixels(
            gridItem = gridItem,
            width = 100,
            height = 100,
            rows = 4,
            columns = 5,
            gridWidth = 500,
            gridHeight = 400,
            anchor = SideAnchor.Left,
        )

        assertEquals(2, result.startColumn)
        assertEquals(1, result.startRow)
        assertEquals(1, result.columnSpan)
        assertEquals(1, result.rowSpan)
    }

    @Test
    fun `keeps right anchor fixed while resizing`() {
        val gridItem = getGridItem(
            startColumn = 2,
            startRow = 1,
            columnSpan = 2,
            rowSpan = 2,
        )

        val result = resizeWidgetGridItemWithPixels(
            gridItem = gridItem,
            width = 100,
            height = 100,
            rows = 4,
            columns = 5,
            gridWidth = 500,
            gridHeight = 400,
            anchor = SideAnchor.Right,
        )

        assertEquals(3, result.startColumn)
        assertEquals(1, result.startRow)
        assertEquals(1, result.columnSpan)
        assertEquals(1, result.rowSpan)
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

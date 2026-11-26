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
import com.eblan.launcher.domain.model.grid.GridItemData
import com.eblan.launcher.domain.model.grid.GridItems
import com.eblan.launcher.domain.repository.FakeGridRepository
import com.eblan.launcher.domain.usecase.util.toGridItems
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class ResetGridItemCustomIconUseCaseTest {
    private val dispatcher = StandardTestDispatcher()

    @Test
    fun `clears custom icon only on grid items using the reset path`() = runTest(dispatcher) {
        val gridRepository = FakeGridRepository(
            initialGridItems = GridItems(
                applicationInfoGridItems = listOf(
                    getApplicationInfoGridItem(id = "same", customIcon = "/icons/a.png"),
                    getApplicationInfoGridItem(id = "other", customIcon = "/icons/b.png"),
                ),
                widgetGridItems = emptyList(),
                shortcutInfoGridItems = emptyList(),
                shortcutConfigGridItems = emptyList(),
                folderGridItems = emptyList(),
            ),
        )

        val resetGridItemCustomIconUseCase = ResetGridItemCustomIconUseCase(
            gridRepository = gridRepository,
            ioDispatcher = dispatcher,
        )

        resetGridItemCustomIconUseCase(customIcon = "/icons/a.png")

        val gridItems = gridRepository.getGridItems().toGridItems()

        assertEquals(null, (gridItems.single { it.id == "same" }.data as GridItemData.ApplicationInfo).customIcon)
        assertEquals("/icons/b.png", (gridItems.single { it.id == "other" }.data as GridItemData.ApplicationInfo).customIcon)
    }

    private fun getApplicationInfoGridItem(
        id: String,
        customIcon: String?,
    ) = ApplicationInfoGridItem(
        id = id,
        page = 0,
        startColumn = 0,
        startRow = 0,
        columnSpan = 1,
        rowSpan = 1,
        associate = Associate.Grid,
        componentName = "com.example/.MainActivity",
        packageName = "com.example",
        icon = null,
        label = "Example",
        override = false,
        serialNumber = 0L,
        customIcon = customIcon,
        customLabel = null,
        gridItemSettings = getGridItemSettings(),
        doubleTap = getEblanAction(),
        swipeUp = getEblanAction(),
        swipeDown = getEblanAction(),
        index = 0,
        folderId = null,
    )
}

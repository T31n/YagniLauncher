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
package com.eblan.launcher.domain.repository

import com.eblan.launcher.domain.model.grid.ApplicationInfoGridItem
import com.eblan.launcher.domain.model.grid.FolderGridItem
import com.eblan.launcher.domain.model.grid.GridItem
import com.eblan.launcher.domain.model.grid.GridItemData
import com.eblan.launcher.domain.model.grid.GridItems
import com.eblan.launcher.domain.model.grid.ShortcutConfigGridItem
import com.eblan.launcher.domain.model.grid.ShortcutInfoGridItem
import com.eblan.launcher.domain.model.grid.WidgetGridItem
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update

class FakeGridRepository(initialGridItems: GridItems) : GridRepository {
    private val _gridItemsFlow = MutableStateFlow(initialGridItems)

    override val gridItemsFlow: Flow<GridItems>
        get() = _gridItemsFlow

    override suspend fun getGridItems(): GridItems = _gridItemsFlow.value

    override suspend fun insertGridItem(gridItem: GridItem) {
        _gridItemsFlow.update { current ->
            current.add(gridItem)
        }
    }

    override suspend fun updateGridItem(gridItem: GridItem) {
        _gridItemsFlow.update { current ->
            current.update(gridItem)
        }
    }

    override suspend fun upsertGridItems(gridItems: List<GridItem>) {
        _gridItemsFlow.update { current ->
            gridItems.fold(current) { result, gridItem ->
                result.upsert(gridItem)
            }
        }
    }

    override suspend fun deleteGridItems(gridItems: List<GridItem>) {
        _gridItemsFlow.update { current ->
            gridItems.fold(current) { result, gridItem ->
                result.deleteById(gridItem.id)
            }
        }
    }

    override suspend fun deleteGridItem(gridItem: GridItem) {
        _gridItemsFlow.update { current ->
            current.deleteById(gridItem.id)
        }
    }

    override suspend fun upsertGridItem(gridItem: GridItem) {
        _gridItemsFlow.update { current ->
            current.upsert(gridItem)
        }
    }

    override suspend fun deleteGridItemById(gridItem: GridItem) {
        _gridItemsFlow.update { current ->
            current.deleteById(gridItem.id)
        }
    }

    private fun GridItems.add(
        gridItem: GridItem,
    ): GridItems = when (val data = gridItem.data) {
        is GridItemData.ApplicationInfo -> copy(
            applicationInfoGridItems =
            applicationInfoGridItems + ApplicationInfoGridItem(
                id = gridItem.id,
                page = gridItem.page,
                startColumn = gridItem.startColumn,
                startRow = gridItem.startRow,
                columnSpan = gridItem.columnSpan,
                rowSpan = gridItem.rowSpan,
                associate = gridItem.associate,
                componentName = data.componentName,
                packageName = data.packageName,
                icon = data.icon,
                label = data.label,
                override = gridItem.override,
                serialNumber = data.serialNumber,
                customIcon = data.customIcon,
                customLabel = data.customLabel,
                gridItemSettings = gridItem.gridItemSettings,
                doubleTap = gridItem.doubleTap,
                swipeUp = gridItem.swipeUp,
                swipeDown = gridItem.swipeDown,
                index = data.index,
                folderId = data.folderId,
            ),
        )

        is GridItemData.Widget -> copy(
            widgetGridItems =
            widgetGridItems + WidgetGridItem(
                id = gridItem.id,
                page = gridItem.page,
                startColumn = gridItem.startColumn,
                startRow = gridItem.startRow,
                columnSpan = gridItem.columnSpan,
                rowSpan = gridItem.rowSpan,
                associate = gridItem.associate,
                appWidgetId = data.appWidgetId,
                packageName = data.packageName,
                serialNumber = data.serialNumber,
                componentName = data.componentName,
                configure = data.configure,
                minWidth = data.minWidth,
                minHeight = data.minHeight,
                resizeMode = data.resizeMode,
                minResizeWidth = data.minResizeWidth,
                minResizeHeight = data.minResizeHeight,
                maxResizeWidth = data.maxResizeWidth,
                maxResizeHeight = data.maxResizeHeight,
                targetCellHeight = data.targetCellHeight,
                targetCellWidth = data.targetCellWidth,
                preview = data.preview,
                label = data.label,
                icon = data.icon,
                override = gridItem.override,
                gridItemSettings = gridItem.gridItemSettings,
            ),
        )

        is GridItemData.ShortcutInfo -> copy(
            shortcutInfoGridItems =
            shortcutInfoGridItems + ShortcutInfoGridItem(
                id = gridItem.id,
                page = gridItem.page,
                startColumn = gridItem.startColumn,
                startRow = gridItem.startRow,
                columnSpan = gridItem.columnSpan,
                rowSpan = gridItem.rowSpan,
                associate = gridItem.associate,
                shortcutId = data.shortcutId,
                packageName = data.packageName,
                shortLabel = data.shortLabel,
                longLabel = data.longLabel,
                icon = data.icon,
                override = gridItem.override,
                serialNumber = data.serialNumber,
                isEnabled = data.isEnabled,
                eblanApplicationInfoIcon = data.eblanApplicationInfoIcon,
                customIcon = data.customIcon,
                customShortLabel = data.customShortLabel,
                gridItemSettings = gridItem.gridItemSettings,
                doubleTap = gridItem.doubleTap,
                swipeUp = gridItem.swipeUp,
                swipeDown = gridItem.swipeDown,
                index = data.index,
                folderId = data.folderId,
            ),
        )

        is GridItemData.ShortcutConfig -> copy(
            shortcutConfigGridItems =
            shortcutConfigGridItems + ShortcutConfigGridItem(
                id = gridItem.id,
                page = gridItem.page,
                startColumn = gridItem.startColumn,
                startRow = gridItem.startRow,
                columnSpan = gridItem.columnSpan,
                rowSpan = gridItem.rowSpan,
                associate = gridItem.associate,
                componentName = data.componentName,
                packageName = data.packageName,
                activityIcon = data.activityIcon,
                activityLabel = data.activityLabel,
                applicationIcon = data.applicationIcon,
                applicationLabel = data.applicationLabel,
                override = gridItem.override,
                serialNumber = data.serialNumber,
                shortcutIntentName = data.shortcutIntentName,
                shortcutIntentIcon = data.shortcutIntentIcon,
                shortcutIntentUri = data.shortcutIntentUri,
                customIcon = data.customIcon,
                customLabel = data.customLabel,
                gridItemSettings = gridItem.gridItemSettings,
                doubleTap = gridItem.doubleTap,
                swipeUp = gridItem.swipeUp,
                swipeDown = gridItem.swipeDown,
                index = data.index,
                folderId = data.folderId,
            ),
        )

        is GridItemData.Folder -> copy(
            folderGridItems =
            folderGridItems + FolderGridItem(
                id = gridItem.id,
                page = gridItem.page,
                startColumn = gridItem.startColumn,
                startRow = gridItem.startRow,
                columnSpan = gridItem.columnSpan,
                rowSpan = gridItem.rowSpan,
                associate = gridItem.associate,
                label = data.label,
                override = gridItem.override,
                icon = data.icon,
                gridItemSettings = gridItem.gridItemSettings,
                doubleTap = gridItem.doubleTap,
                swipeUp = gridItem.swipeUp,
                swipeDown = gridItem.swipeDown,
                index = data.index,
                folderId = data.folderId,
            ),
        )
    }

    private fun GridItems.update(
        gridItem: GridItem,
    ): GridItems = deleteById(gridItem.id).add(gridItem)

    private fun GridItems.upsert(
        gridItem: GridItem,
    ): GridItems = if (contains(gridItem.id)) {
        update(gridItem)
    } else {
        add(gridItem)
    }

    private fun GridItems.contains(
        id: String,
    ): Boolean = applicationInfoGridItems.any { it.id == id } ||
        widgetGridItems.any { it.id == id } ||
        shortcutInfoGridItems.any { it.id == id } ||
        shortcutConfigGridItems.any { it.id == id } ||
        folderGridItems.any { it.id == id }

    private fun GridItems.deleteById(
        id: String,
    ): GridItems = copy(
        applicationInfoGridItems =
        applicationInfoGridItems.filterNot { it.id == id },
        widgetGridItems =
        widgetGridItems.filterNot { it.id == id },
        shortcutInfoGridItems =
        shortcutInfoGridItems.filterNot { it.id == id },
        shortcutConfigGridItems =
        shortcutConfigGridItems.filterNot { it.id == id },
        folderGridItems =
        folderGridItems.filterNot { it.id == id },
    )
}

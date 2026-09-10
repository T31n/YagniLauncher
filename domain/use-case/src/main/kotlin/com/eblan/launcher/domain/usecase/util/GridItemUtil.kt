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
package com.eblan.launcher.domain.usecase.util

import com.eblan.launcher.domain.common.FileManager
import com.eblan.launcher.domain.common.IconKeyGenerator
import com.eblan.launcher.domain.framework.AppWidgetHostWrapper
import com.eblan.launcher.domain.framework.LauncherAppsWrapper
import com.eblan.launcher.domain.model.GridItem
import com.eblan.launcher.domain.model.GridItemData
import com.eblan.launcher.domain.model.GridItems
import com.eblan.launcher.domain.model.ShortcutQuery
import com.eblan.launcher.domain.model.ShortcutQueryFlag
import com.eblan.launcher.domain.repository.FolderGridItemRepository
import com.eblan.launcher.domain.usecase.grid.FOLDER_PREVIEW_COLUMNS
import com.eblan.launcher.domain.usecase.grid.FOLDER_PREVIEW_ROWS
import com.eblan.launcher.domain.usecase.grid.asGridItem

internal suspend fun deleteGridItemData(
    gridItem: GridItem,
    appWidgetHostWrapper: AppWidgetHostWrapper,
    launcherAppsWrapper: LauncherAppsWrapper,
    folderGridItemRepository: FolderGridItemRepository,
    fileManager: FileManager,
    iconKeyGenerator: IconKeyGenerator,
    iconPackInfoPackageName: String,
) {
    when (val data = gridItem.data) {
        is GridItemData.ShortcutInfo -> updatePinShortcutsByPackageName(launcherAppsWrapper, data)

        is GridItemData.Widget -> appWidgetHostWrapper.deleteAppWidgetId(data.appWidgetId)

        is GridItemData.Folder -> {
            val folderGridItems = getFolderGridItemsById(
                folderGridItemRepository = folderGridItemRepository,
                folderId = gridItem.id,
            )

            folderGridItems.forEach { folderGridItem ->
                deleteGridItemData(
                    gridItem = folderGridItem,
                    appWidgetHostWrapper = appWidgetHostWrapper,
                    launcherAppsWrapper = launcherAppsWrapper,
                    folderGridItemRepository = folderGridItemRepository,
                    fileManager = fileManager,
                    iconKeyGenerator = iconKeyGenerator,
                    iconPackInfoPackageName = iconPackInfoPackageName,
                )
            }
        }

        else -> Unit
    }
}

suspend fun getFolderGridItemsById(
    folderGridItemRepository: FolderGridItemRepository,
    folderId: String,
): List<GridItem> {
    val folderGridItemWrapper = folderGridItemRepository.getFolderGridItemWrapper(
        id = folderId,
    ) ?: return emptyList()

    val childFolderGridItems = folderGridItemWrapper.folderGridItems.map { folderGridItem ->
        folderGridItemRepository.getFolderGridItemWrapper(
            id = folderGridItem.id,
        )?.asGridItem() ?: folderGridItem.asGridItem()
    }

    return (
        folderGridItemWrapper.applicationInfoGridItems.map {
            it.asGridItem()
        } + folderGridItemWrapper.shortcutInfoGridItems.map {
            it.asGridItem()
        } + folderGridItemWrapper.shortcutConfigGridItems.map {
            it.asGridItem()
        } + childFolderGridItems
        ).sortedBy { gridItem ->
        when (val data = gridItem.data) {
            is GridItemData.ApplicationInfo -> data.index
            is GridItemData.ShortcutInfo -> data.index
            is GridItemData.ShortcutConfig -> data.index
            is GridItemData.Folder -> data.index
            else -> error("Unsupported folder grid item")
        }
    }
}

internal fun GridItems.toGridItems(): List<GridItem> = buildList {
    addAll(
        applicationInfoGridItems.map {
            it.asGridItem()
        },
    )
    addAll(widgetGridItems.map { it.asGridItem() })
    addAll(shortcutInfoGridItems.map { it.asGridItem() })
    addAll(shortcutConfigGridItems.map { it.asGridItem() })
    addAll(folderGridItems.map { it.asGridItem() })
}

internal fun GridItem.isTopLevel() = when (val itemData = data) {
    is GridItemData.ApplicationInfo -> itemData.folderId == null
    is GridItemData.Folder -> itemData.folderId == null
    is GridItemData.ShortcutConfig -> itemData.folderId == null
    is GridItemData.ShortcutInfo -> itemData.folderId == null
    is GridItemData.Widget -> true
}

internal fun <T> getPreviewFolderGridItems(
    rows: Int,
    columns: Int,
    folderGridItems: List<T>,
): List<T> = buildList {
    for (row in 0 until minOf(rows, FOLDER_PREVIEW_ROWS)) {
        for (column in 0 until minOf(columns, FOLDER_PREVIEW_COLUMNS)) {
            val index = row * columns + column

            folderGridItems.getOrNull(index)?.let(::add)
        }
    }
}

private suspend fun updatePinShortcutsByPackageName(
    launcherAppsWrapper: LauncherAppsWrapper,
    data: GridItemData.ShortcutInfo,
) {
    if (!launcherAppsWrapper.hasShortcutHostPermission) return

    val shortcutIds = launcherAppsWrapper.getFastShortcuts(
        shortcutQuery = ShortcutQuery(
            packageName = data.packageName,
            shortcutQueryFlag = ShortcutQueryFlag.Pinned,
        ),
    )?.map { it.shortcutId } ?: return

    if (data.shortcutId !in shortcutIds) return

    launcherAppsWrapper.pinShortcuts(
        packageName = data.packageName,
        shortcutIds = shortcutIds - data.shortcutId,
        serialNumber = data.serialNumber,
    )
}

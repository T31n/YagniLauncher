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

import com.eblan.launcher.domain.framework.AppWidgetHostWrapper
import com.eblan.launcher.domain.framework.LauncherAppsWrapper
import com.eblan.launcher.domain.model.folder.FolderPopup
import com.eblan.launcher.domain.model.grid.ApplicationInfoGridItem
import com.eblan.launcher.domain.model.grid.FolderGridItem
import com.eblan.launcher.domain.model.grid.GridItem
import com.eblan.launcher.domain.model.grid.GridItemData
import com.eblan.launcher.domain.model.grid.GridItems
import com.eblan.launcher.domain.model.grid.ShortcutConfigGridItem
import com.eblan.launcher.domain.model.grid.ShortcutInfoGridItem
import com.eblan.launcher.domain.model.grid.WidgetGridItem
import com.eblan.launcher.domain.model.launcherapps.ShortcutQuery
import com.eblan.launcher.domain.model.launcherapps.ShortcutQueryFlag
import com.eblan.launcher.domain.model.userdata.EblanAction
import com.eblan.launcher.domain.model.userdata.EblanActionType
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlin.math.ceil
import kotlin.math.min
import kotlin.math.sqrt

const val FOLDER_PREVIEW_COLUMNS = 2
const val FOLDER_PREVIEW_ROWS = 2

/** Releases external resources owned by a grid item and by items nested in its folder tree. */
internal suspend fun deleteGridItemData(
    gridItem: GridItem,
    folderPopupGridItems: Map<String, FolderPopup>,
    appWidgetHostWrapper: AppWidgetHostWrapper,
    launcherAppsWrapper: LauncherAppsWrapper,
) {
    /** Unpins a shortcut or releases a widget ID when the item owns either resource. */
    suspend fun delete(
        gridItem: GridItem,
        appWidgetHostWrapper: AppWidgetHostWrapper,
        launcherAppsWrapper: LauncherAppsWrapper,
    ) {
        when (val data = gridItem.data) {
            is GridItemData.ShortcutInfo -> updatePinShortcutsByPackageName(
                launcherAppsWrapper,
                data,
            )

            is GridItemData.Widget -> appWidgetHostWrapper.deleteAppWidgetId(data.appWidgetId)

            else -> Unit
        }
    }

    when (gridItem.data) {
        is GridItemData.ApplicationInfo,
        is GridItemData.ShortcutConfig,
        is GridItemData.ShortcutInfo,
        is GridItemData.Widget,
        -> delete(
            gridItem = gridItem,
            appWidgetHostWrapper = appWidgetHostWrapper,
            launcherAppsWrapper = launcherAppsWrapper,
        )

        is GridItemData.Folder -> getRecursiveFolderGridItems(
            folderPopupGridItems = folderPopupGridItems,
            folderId = gridItem.id,
        ).forEach {
            delete(
                gridItem = it,
                appWidgetHostWrapper = appWidgetHostWrapper,
                launcherAppsWrapper = launcherAppsWrapper,
            )
        }
    }
}

/**
 * Recursively retrieves the grid items from a preview folder and its nested folders.
 *
 * @param gridItem The current grid item representing the folder.
 * @param folderPopupGridItems A map of preview folder grid items by their IDs.
 * @param folderId The ID of the folder whose contents should be traversed.
 * @return A list of grid items contained within the specified folder and its nested folders.
 */
internal fun getRecursiveFolderGridItems(
    gridItem: GridItem? = null,
    folderPopupGridItems: Map<String, FolderPopup>,
    folderId: String,
): List<GridItem> = buildList {
    val previewFolder = folderPopupGridItems[folderId] ?: return@buildList

    if (gridItem != null) {
        add(gridItem)
    }

    previewFolder.folderGridItems.forEach { folderGridItem ->
        when (folderGridItem.data) {
            is GridItemData.ApplicationInfo,
            is GridItemData.ShortcutInfo,
            is GridItemData.ShortcutConfig,
            is GridItemData.Widget,
            -> {
                add(folderGridItem)
            }

            is GridItemData.Folder -> {
                addAll(
                    getRecursiveFolderGridItems(
                        gridItem = folderGridItem,
                        folderPopupGridItems = folderPopupGridItems,
                        folderId = folderGridItem.id,
                    ),
                )
            }
        }
    }
}

/** Converts the repository's grid-item collections into a single domain grid-item list. */
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

/** Returns whether this item is not contained in another folder. */
internal fun GridItem.isTopLevel() = when (val itemData = data) {
    is GridItemData.ApplicationInfo -> itemData.folderId == null
    is GridItemData.Folder -> itemData.folderId == null
    is GridItemData.ShortcutConfig -> itemData.folderId == null
    is GridItemData.ShortcutInfo -> itemData.folderId == null
    is GridItemData.Widget -> true
}

/** Selects the items displayed in the leading preview cells of a folder. */
internal suspend fun <T> getPreviewFolderGridItems(
    columns: Int,
    rows: Int,
    gridItems: List<T>,
): List<T> = buildList {
    for (row in 0 until minOf(rows, FOLDER_PREVIEW_ROWS)) {
        currentCoroutineContext().ensureActive()

        for (column in 0 until minOf(columns, FOLDER_PREVIEW_COLUMNS)) {
            currentCoroutineContext().ensureActive()

            val index = row * columns + column

            gridItems.getOrNull(index)?.let(::add)
        }
    }
}

/**
 * Chooses a compact grid dimension that can contain [count] items without exceeding either limit.
 *
 * @return The selected column count followed by the row count, or `0 to 0` for an empty grid.
 */
internal fun getGridDimension(
    count: Int,
    maxFolderColumns: Int,
    maxFolderRows: Int,
): Pair<Int, Int> {
    if (count <= 0) return 0 to 0

    val columns = min(maxFolderColumns, ceil(sqrt(count.toDouble())).toInt())
    val rows = min(maxFolderRows, ceil(count / columns.toDouble()).toInt())

    return columns to rows
}

/** Splits items into sequential pages with at most [columns] times [rows] items per page. */
internal suspend fun <T> List<T>.getGridItemsByPage(
    columns: Int,
    rows: Int,
): Map<Int, List<T>> {
    if (isEmpty()) return emptyMap()

    return chunked(columns * rows).mapIndexed { index, gridItems ->
        currentCoroutineContext().ensureActive()

        index to gridItems
    }.toMap()
}

/** Converts a persisted application grid item to its domain representation. */
internal fun ApplicationInfoGridItem.asGridItem(): GridItem = GridItem(
    id = id,
    page = page,
    startColumn = startColumn,
    startRow = startRow,
    columnSpan = columnSpan,
    rowSpan = rowSpan,
    data = GridItemData.ApplicationInfo(
        serialNumber = serialNumber,
        componentName = componentName,
        packageName = packageName,
        icon = icon,
        label = label,
        customIcon = customIcon,
        customLabel = customLabel,
        index = index,
        folderId = folderId,
    ),
    associate = associate,
    override = override,
    gridItemSettings = gridItemSettings,
    doubleTap = doubleTap,
    swipeUp = swipeUp,
    swipeDown = swipeDown,
)

/** Converts a persisted widget grid item to its domain representation. */
internal fun WidgetGridItem.asGridItem(): GridItem = GridItem(
    id = id,
    page = page,
    startColumn = startColumn,
    startRow = startRow,
    columnSpan = columnSpan,
    rowSpan = rowSpan,
    data = GridItemData.Widget(
        appWidgetId = appWidgetId,
        componentName = componentName,
        packageName = packageName,
        serialNumber = serialNumber,
        configure = configure,
        minWidth = minWidth,
        minHeight = minHeight,
        resizeMode = resizeMode,
        minResizeWidth = minResizeWidth,
        minResizeHeight = minResizeHeight,
        maxResizeWidth = maxResizeWidth,
        maxResizeHeight = maxResizeHeight,
        targetCellHeight = targetCellHeight,
        targetCellWidth = targetCellWidth,
        preview = preview,
        label = label,
        icon = icon,
    ),
    associate = associate,
    override = override,
    gridItemSettings = gridItemSettings,
    doubleTap = EblanAction(
        eblanActionType = EblanActionType.None,
        serialNumber = 0L,
        componentName = "",
    ),
    swipeUp = EblanAction(
        eblanActionType = EblanActionType.None,
        serialNumber = 0L,
        componentName = "",
    ),
    swipeDown = EblanAction(
        eblanActionType = EblanActionType.None,
        serialNumber = 0L,
        componentName = "",
    ),
)

/** Converts a persisted shortcut grid item to its domain representation. */
internal fun ShortcutInfoGridItem.asGridItem(): GridItem = GridItem(
    id = id,
    page = page,
    startColumn = startColumn,
    startRow = startRow,
    columnSpan = columnSpan,
    rowSpan = rowSpan,
    data = GridItemData.ShortcutInfo(
        shortcutId = shortcutId,
        packageName = packageName,
        serialNumber = serialNumber,
        shortLabel = shortLabel,
        longLabel = longLabel,
        icon = icon,
        isEnabled = isEnabled,
        eblanApplicationInfoIcon = eblanApplicationInfoIcon,
        customIcon = customIcon,
        customShortLabel = customShortLabel,
        index = index,
        folderId = folderId,
    ),
    associate = associate,
    override = override,
    gridItemSettings = gridItemSettings,
    doubleTap = doubleTap,
    swipeUp = swipeUp,
    swipeDown = swipeDown,
)

/** Converts a persisted shortcut-configuration grid item to its domain representation. */
internal fun ShortcutConfigGridItem.asGridItem(): GridItem = GridItem(
    id = id,
    page = page,
    startColumn = startColumn,
    startRow = startRow,
    columnSpan = columnSpan,
    rowSpan = rowSpan,
    data = GridItemData.ShortcutConfig(
        serialNumber = serialNumber,
        componentName = componentName,
        packageName = packageName,
        activityIcon = activityIcon,
        activityLabel = activityLabel,
        applicationIcon = applicationIcon,
        applicationLabel = applicationLabel,
        shortcutIntentName = shortcutIntentName,
        shortcutIntentIcon = shortcutIntentIcon,
        shortcutIntentUri = shortcutIntentUri,
        customIcon = customIcon,
        customLabel = customLabel,
        index = index,
        folderId = folderId,
    ),
    associate = associate,
    override = override,
    gridItemSettings = gridItemSettings,
    doubleTap = doubleTap,
    swipeUp = swipeUp,
    swipeDown = swipeDown,
)

/** Converts a persisted folder grid item to its domain representation. */
internal fun FolderGridItem.asGridItem(): GridItem = GridItem(
    id = id,
    page = page,
    startColumn = startColumn,
    startRow = startRow,
    columnSpan = columnSpan,
    rowSpan = rowSpan,
    data = GridItemData.Folder(
        label = label,
        icon = icon,
        index = index,
        folderId = folderId,
    ),
    associate = associate,
    override = override,
    gridItemSettings = gridItemSettings,
    doubleTap = doubleTap,
    swipeUp = swipeUp,
    swipeDown = swipeDown,
)

/** Removes a shortcut from the launcher's pinned shortcut list when permission is available. */
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

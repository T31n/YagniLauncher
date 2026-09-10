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

import com.eblan.launcher.domain.model.ApplicationInfoGridItem
import com.eblan.launcher.domain.model.EblanAction
import com.eblan.launcher.domain.model.EblanActionType
import com.eblan.launcher.domain.model.FolderGridItem
import com.eblan.launcher.domain.model.FolderGridItemWrapper
import com.eblan.launcher.domain.model.GridItem
import com.eblan.launcher.domain.model.GridItemData
import com.eblan.launcher.domain.model.ShortcutConfigGridItem
import com.eblan.launcher.domain.model.ShortcutInfoGridItem
import com.eblan.launcher.domain.model.WidgetGridItem
import java.io.File
import kotlin.math.ceil
import kotlin.math.min
import kotlin.math.sqrt

const val FOLDER_PREVIEW_COLUMNS = 2
const val FOLDER_PREVIEW_ROWS = 2

internal fun deleteGridItemCustomIconFile(gridItem: GridItem) = when (val data = gridItem.data) {
    is GridItemData.ApplicationInfo -> {
        data.customIcon?.let {
            val customIconFile = File(it)

            if (customIconFile.exists()) {
                customIconFile.delete()
            }
        }
    }

    is GridItemData.ShortcutConfig -> {
        data.customIcon?.let {
            val customIconFile = File(it)

            if (customIconFile.exists()) {
                customIconFile.delete()
            }
        }
    }

    is GridItemData.ShortcutInfo -> {
        data.customIcon?.let {
            val customIconFile = File(it)

            if (customIconFile.exists()) {
                customIconFile.delete()
            }
        }
    }

    is GridItemData.Folder -> {
        data.icon?.let {
            val iconFile = File(it)

            if (iconFile.exists()) {
                iconFile.delete()
            }
        }
    }

    else -> Unit
}

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

internal fun FolderGridItemWrapper.asGridItem(): GridItem = GridItem(
    id = folderGridItem.id,
    page = folderGridItem.page,
    startColumn = folderGridItem.startColumn,
    startRow = folderGridItem.startRow,
    columnSpan = folderGridItem.columnSpan,
    rowSpan = folderGridItem.rowSpan,
    data = GridItemData.Folder(
        label = folderGridItem.label,
        icon = folderGridItem.icon,
        index = folderGridItem.index,
        folderId = folderGridItem.folderId,
    ),
    associate = folderGridItem.associate,
    override = folderGridItem.override,
    gridItemSettings = folderGridItem.gridItemSettings,
    doubleTap = folderGridItem.doubleTap,
    swipeUp = folderGridItem.swipeUp,
    swipeDown = folderGridItem.swipeDown,
)

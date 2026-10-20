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
package com.eblan.launcher.domain.usecase.folder

import com.eblan.launcher.domain.model.application.EblanApplicationInfo
import com.eblan.launcher.domain.model.folder.FolderEblanApplicationInfo
import com.eblan.launcher.domain.model.folder.FolderEblanApplicationInfoGridItem
import com.eblan.launcher.domain.model.folder.FolderEblanApplicationInfoGridItemData
import com.eblan.launcher.domain.model.folder.FolderEblanApplicationInfoPopup
import com.eblan.launcher.domain.model.folder.FolderEblanApplicationInfoWrapper
import com.eblan.launcher.domain.model.folder.FolderPopup
import com.eblan.launcher.domain.model.grid.FolderGridItemWrapper
import com.eblan.launcher.domain.model.grid.GridItemData
import com.eblan.launcher.domain.usecase.util.asGridItem
import com.eblan.launcher.domain.usecase.util.getGridDimension
import com.eblan.launcher.domain.usecase.util.getGridItemsByPage
import com.eblan.launcher.domain.usecase.util.getPreviewFolderGridItems
import kotlin.collections.orEmpty

/** Converts a stored folder record to its folder-grid-item representation. */
internal fun FolderEblanApplicationInfo.asFolderEblanApplicationInfoGridItem(): FolderEblanApplicationInfoGridItem = FolderEblanApplicationInfoGridItem(
    id = id,
    data = FolderEblanApplicationInfoGridItemData.Folder(
        icon = icon,
        label = label,
        index = index,
        folderIndex = folderIndex,
        folderId = folderId,
    ),
)

/** Converts a stored application record to its folder-grid-item representation. */
internal fun EblanApplicationInfo.asFolderEblanApplicationInfoGridItem(): FolderEblanApplicationInfoGridItem = FolderEblanApplicationInfoGridItem(
    id = "$componentName $serialNumber",
    data = FolderEblanApplicationInfoGridItemData.ApplicationInfo(
        componentName = componentName,
        serialNumber = serialNumber,
        packageName = packageName,
        icon = icon,
        label = label,
        customIcon = customIcon,
        customLabel = customLabel,
        isHidden = isHidden,
        lastUpdateTime = lastUpdateTime,
        flags = flags,
        folderIndex = folderIndex,
        folderId = folderId,
    ),
)

/** Builds a preview popup for each stored folder and indexes the results by folder ID. */
internal suspend fun List<FolderEblanApplicationInfoWrapper>.asPreviewFolderEblanApplicationInfos(
    maxFolderColumns: Int,
    maxFolderRows: Int,
): Map<String, FolderEblanApplicationInfoPopup> = associate {
    it.folderEblanApplicationInfo.id to it.asPreviewFolderEblanApplicationInfo(
        maxFolderColumns = maxFolderColumns,
        maxFolderRows = maxFolderRows,
    )
}

/** Builds a preview popup for each grid folder and indexes the results by folder ID. */
internal suspend fun List<FolderGridItemWrapper>.asPreviewFolders(
    maxFolderColumns: Int,
    maxFolderRows: Int,
): Map<String, FolderPopup> = associate {
    it.folderGridItem.id to it.asPreviewFolder(
        maxFolderColumns = maxFolderColumns,
        maxFolderRows = maxFolderRows,
    )
}

/**
 * Converts a folder wrapper into a popup containing ordered children, page groups, and preview
 * items.
 */
internal suspend fun FolderGridItemWrapper.asPreviewFolder(
    maxFolderColumns: Int,
    maxFolderRows: Int,
): FolderPopup {
    val folderGridItems = (
        applicationInfoGridItems.map {
            it.asGridItem()
        } + shortcutInfoGridItems.map { it.asGridItem() } +
            shortcutConfigGridItems.map { it.asGridItem() } +
            folderGridItems.map { it.asGridItem() }
        ).sortedBy {
        when (val data = it.data) {
            is GridItemData.ApplicationInfo -> data.index
            is GridItemData.ShortcutInfo -> data.index
            is GridItemData.ShortcutConfig -> data.index
            is GridItemData.Folder -> data.index
            else -> error("Unsupported folder grid item")
        }
    }

    val (columns, rows) = getGridDimension(
        count = folderGridItems.size,
        maxFolderColumns = maxFolderColumns,
        maxFolderRows = maxFolderRows,
    )

    val folderGridItemsByPage = folderGridItems.getGridItemsByPage(
        columns = columns,
        rows = rows,
    )

    val firstPageFolderGridItems = folderGridItemsByPage.values.firstOrNull().orEmpty()

    val previewFolderGridItems = getPreviewFolderGridItems(
        columns = columns,
        rows = rows,
        gridItems = firstPageFolderGridItems,
    )

    return FolderPopup(
        folderGridItem = folderGridItem,
        previewFolderGridItems = previewFolderGridItems,
        folderGridItems = folderGridItems,
        folderGridItemsByPage = folderGridItemsByPage,
        columns = columns,
        rows = rows,
    )
}

/** Converts an application-folder wrapper into a popup with its nested folder contents. */
private suspend fun FolderEblanApplicationInfoWrapper.asPreviewFolderEblanApplicationInfo(
    maxFolderColumns: Int,
    maxFolderRows: Int,
): FolderEblanApplicationInfoPopup {
    val folderEblanApplicationInfoGridItems = (
        folderEblanApplicationInfos.map {
            it.asFolderEblanApplicationInfoGridItem()
        } + eblanApplicationInfos.map {
            it.asFolderEblanApplicationInfoGridItem()
        }
        ).sortedBy {
        when (val data = it.data) {
            is FolderEblanApplicationInfoGridItemData.ApplicationInfo -> data.folderIndex
            is FolderEblanApplicationInfoGridItemData.Folder -> data.folderIndex
        }
    }

    val (columns, rows) = getGridDimension(
        count = folderEblanApplicationInfoGridItems.size,
        maxFolderColumns = maxFolderColumns,
        maxFolderRows = maxFolderRows,
    )

    val folderEblanApplicationInfoGridItemsByPage =
        folderEblanApplicationInfoGridItems.getGridItemsByPage(
            columns = columns,
            rows = rows,
        )

    val previewFolderGridItems = getPreviewFolderGridItems(
        columns = columns,
        rows = rows,
        gridItems = folderEblanApplicationInfoGridItems,
    )

    return FolderEblanApplicationInfoPopup(
        folderEblanApplicationInfo = folderEblanApplicationInfo,
        previewFolderEblanApplicationInfoGridItems = previewFolderGridItems,
        folderEblanApplicationInfoGridItems = folderEblanApplicationInfoGridItems,
        folderEblanApplicationInfoGridItemsByPage = folderEblanApplicationInfoGridItemsByPage,
        columns = columns,
        rows = rows,
    )
}

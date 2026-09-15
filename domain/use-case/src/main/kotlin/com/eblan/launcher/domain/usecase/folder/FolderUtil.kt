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
import com.eblan.launcher.domain.model.folder.FolderEblanApplicationInfoWrapper
import com.eblan.launcher.domain.model.folder.PreviewFolder
import com.eblan.launcher.domain.model.folder.PreviewFolderEblanApplicationInfo
import com.eblan.launcher.domain.model.grid.FolderGridItemWrapper
import com.eblan.launcher.domain.model.grid.GridItemData
import com.eblan.launcher.domain.usecase.util.asGridItem
import com.eblan.launcher.domain.usecase.util.getGridDimension
import com.eblan.launcher.domain.usecase.util.getPreviewFolderGridItems

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

internal fun List<FolderEblanApplicationInfoWrapper>.asPreviewFolderEblanApplicationInfos(
    maxFolderColumns: Int,
    maxFolderRows: Int,
): Map<String, PreviewFolderEblanApplicationInfo> = associate {
    it.folderEblanApplicationInfo.id to it.asPreviewFolderEblanApplicationInfo(
        maxFolderColumns = maxFolderColumns,
        maxFolderRows = maxFolderRows,
    )
}

internal fun List<FolderGridItemWrapper>.asPreviewFolders(
    maxFolderColumns: Int,
    maxFolderRows: Int,
): Map<String, PreviewFolder> = associate {
    it.folderGridItem.id to it.asPreviewFolder(
        maxFolderColumns = maxFolderColumns,
        maxFolderRows = maxFolderRows,
    )
}

internal fun FolderGridItemWrapper.asPreviewFolder(
    maxFolderColumns: Int,
    maxFolderRows: Int,
): PreviewFolder {
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

    val previewFolderGridItems = getPreviewFolderGridItems(
        columns = columns,
        rows = rows,
        folderGridItems = folderGridItems,
    )

    return PreviewFolder(
        previewFolderGridItems = previewFolderGridItems,
        folderGridItems = folderGridItems,
    )
}

private fun FolderEblanApplicationInfoWrapper.asPreviewFolderEblanApplicationInfo(
    maxFolderColumns: Int,
    maxFolderRows: Int,
): PreviewFolderEblanApplicationInfo {
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

    val previewFolderGridItems = getPreviewFolderGridItems(
        columns = columns,
        rows = rows,
        folderGridItems = folderEblanApplicationInfoGridItems,
    )

    return PreviewFolderEblanApplicationInfo(
        folderEblanApplicationInfo = folderEblanApplicationInfo,
        previewFolderGridItems = previewFolderGridItems,
        folderGridItems = folderEblanApplicationInfoGridItems,
    )
}

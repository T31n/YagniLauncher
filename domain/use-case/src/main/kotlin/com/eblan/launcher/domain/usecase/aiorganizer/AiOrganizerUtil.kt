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
package com.eblan.launcher.domain.usecase.aiorganizer

import com.eblan.launcher.domain.model.application.EblanApplicationInfo
import com.eblan.launcher.domain.model.application.EblanApplicationInfoTag
import com.eblan.launcher.domain.model.application.EblanApplicationInfoTagCrossRef
import com.eblan.launcher.domain.model.grid.Associate
import com.eblan.launcher.domain.model.grid.GridItem
import com.eblan.launcher.domain.model.grid.GridItemData
import com.eblan.launcher.domain.model.grid.GridItemSettings
import com.eblan.launcher.domain.model.grid.GridItems
import com.eblan.launcher.domain.model.userdata.HomeSettings
import com.eblan.launcher.domain.repository.EblanApplicationInfoRepository
import com.eblan.launcher.domain.usecase.util.toGridItems
import java.io.File
import java.io.IOException

/** Writes via a temp file and rename so a reader never sees a partially written home file. */
internal fun writeHomeFile(directory: File, name: String, content: ByteArray) {
    val targetFile = File(directory, name)
    val temporaryFile = File.createTempFile("$name.", ".tmp", directory)

    try {
        temporaryFile.writeBytes(content)

        if (!temporaryFile.renameTo(targetFile)) {
            throw IOException("Unable to move ${temporaryFile.name} to ${targetFile.name}")
        }
    } finally {
        temporaryFile.delete()
    }
}

/**
 * Describes the current home state that an AI organization must be consistent with.
 *
 * @property pageCount Current number of grid pages. The AI cannot change it.
 * @property columns Columns per grid page.
 * @property rows Rows per grid page.
 * @property dockPageCount Number of dock pages, which the AI cannot change.
 * @property dockColumns Columns per dock page.
 * @property dockRows Rows per dock page.
 * @property gridItemSettings Settings applied to newly created grid items.
 * @property gridItems Grid and dock items, including folder children.
 * @property apps All installed applications, including hidden ones that may still be tagged.
 */
internal data class AiOrganizerHomeContext(
    val pageCount: Int,
    val columns: Int,
    val rows: Int,
    val dockPageCount: Int,
    val dockColumns: Int,
    val dockRows: Int,
    val gridItemSettings: GridItemSettings,
    val gridItems: List<GridItem>,
    val apps: List<EblanApplicationInfo>,
) {
    fun columnsOf(associate: Associate): Int = if (associate == Associate.Dock) dockColumns else columns

    fun rowsOf(associate: Associate): Int = if (associate == Associate.Dock) dockRows else rows

    fun pageCountOf(associate: Associate, gridPageCount: Int): Int = if (associate == Associate.Dock) {
        dockPageCount
    } else {
        gridPageCount
    }
}

internal fun buildAiOrganizerHomeContext(
    homeSettings: HomeSettings,
    gridItems: GridItems,
    apps: List<EblanApplicationInfo>,
): AiOrganizerHomeContext = AiOrganizerHomeContext(
    pageCount = homeSettings.pageCount,
    columns = homeSettings.columns,
    rows = homeSettings.rows,
    dockPageCount = homeSettings.dockPageCount,
    dockColumns = homeSettings.dockColumns,
    dockRows = homeSettings.dockRows,
    gridItemSettings = homeSettings.gridItemSettings,
    gridItems = gridItems.toGridItems(),
    apps = apps,
)

internal suspend fun EblanApplicationInfoRepository.getTagCrossRefs(
    tags: List<EblanApplicationInfoTag>,
): List<EblanApplicationInfoTagCrossRef> = tags.flatMap { tag ->
    getEblanApplicationInfosByTagId(tag.id).map {
        EblanApplicationInfoTagCrossRef(
            componentName = it.componentName,
            serialNumber = it.serialNumber,
            id = tag.id,
        )
    }
}

private data class AiItemFields(
    val type: AiGridItemType,
    val componentName: String?,
    val packageName: String?,
    val serialNumber: Long?,
    val label: String?,
)

private fun GridItemData.toAiItemFields(): AiItemFields = when (this) {
    is GridItemData.ApplicationInfo -> AiItemFields(
        type = AiGridItemType.Application,
        componentName = componentName,
        packageName = packageName,
        serialNumber = serialNumber,
        label = customLabel ?: label,
    )

    is GridItemData.Widget -> AiItemFields(
        type = AiGridItemType.Widget,
        componentName = componentName,
        packageName = packageName,
        serialNumber = serialNumber,
        label = label,
    )

    is GridItemData.ShortcutInfo -> AiItemFields(
        type = AiGridItemType.Shortcut,
        componentName = null,
        packageName = packageName,
        serialNumber = serialNumber,
        label = customShortLabel ?: shortLabel,
    )

    is GridItemData.Folder -> AiItemFields(
        type = AiGridItemType.Folder,
        componentName = null,
        packageName = null,
        serialNumber = null,
        label = label,
    )

    is GridItemData.ShortcutConfig -> AiItemFields(
        type = AiGridItemType.ShortcutConfig,
        componentName = componentName,
        packageName = packageName,
        serialNumber = serialNumber,
        label = customLabel ?: activityLabel,
    )
}

internal fun GridItemData.parentFolderId(): String? = when (this) {
    is GridItemData.ApplicationInfo -> folderId
    is GridItemData.ShortcutInfo -> folderId
    is GridItemData.ShortcutConfig -> folderId
    is GridItemData.Folder -> folderId
    is GridItemData.Widget -> null
}

internal fun GridItem.position(context: AiOrganizerHomeContext): Int = startRow * context.columnsOf(associate) + startColumn

private fun EblanApplicationInfo.toAiAppDto(folderId: String? = null): AiAppDto = AiAppDto(
    componentName = componentName,
    packageName = packageName,
    serialNumber = serialNumber,
    label = label,
    folderId = folderId,
)

private fun GridItem.toAiRequestGridItemDto(context: AiOrganizerHomeContext): AiRequestGridItemDto {
    val itemFields = data.toAiItemFields()

    return AiRequestGridItemDto(
        id = id,
        type = itemFields.type,
        componentName = itemFields.componentName,
        packageName = itemFields.packageName,
        serialNumber = itemFields.serialNumber,
        label = itemFields.label,
        page = page,
        position = position(context),
        columnSpan = columnSpan,
        rowSpan = rowSpan,
        folderId = data.parentFolderId(),
    )
}

internal fun AiOrganizerHomeContext.toRequestDto(pageCount: Int): AiOrganizerRequestDto = AiOrganizerRequestDto(
    gridItems = gridItems.filter { it.associate == Associate.Grid }.map { it.toAiRequestGridItemDto(this) },
    dockGridItems = gridItems.filter { it.associate == Associate.Dock }.map { it.toAiRequestGridItemDto(this) },
    appDrawer = apps.filter { !it.isHidden }.map { it.toAiAppDto() },
    settings = AiRequestSettingsDto(
        pageCount = pageCount,
        columns = columns,
        rows = rows,
        dockPageCount = dockPageCount,
        dockColumns = dockColumns,
        dockRows = dockRows,
    ),
)

internal fun GridItem.toAiGridItemDto(context: AiOrganizerHomeContext): AiGridItemDto {
    val itemFields = data.toAiItemFields()

    return AiGridItemDto(
        id = id,
        type = itemFields.type,
        componentName = itemFields.componentName,
        serialNumber = itemFields.serialNumber,
        label = itemFields.label,
        page = page,
        position = position(context),
        folderId = data.parentFolderId(),
    )
}

/**
 * Extracts the JSON document from a model response, tolerating surrounding Markdown code fences.
 */
internal fun parseAiOrganizerHome(raw: String): AiOrganizerHomeDto {
    val trimmedRawResponse = raw.trim()
        .removePrefix("```json")
        .removePrefix("```")
        .removeSuffix("```")
        .trim()

    val jsonStartIndex = trimmedRawResponse.indexOf('{')
    val jsonEndIndex = trimmedRawResponse.lastIndexOf('}')

    require(jsonStartIndex in 0..<jsonEndIndex) { "The AI response does not contain a JSON object" }

    return aiOrganizerJson.decodeFromString<AiOrganizerHomeDto>(
        trimmedRawResponse.substring(jsonStartIndex, jsonEndIndex + 1),
    )
}

/**
 * Removes app drawer folders that have no members and clears references to folders that are not
 * listed, so an empty or dangling folder never fails validation.
 */
internal fun AiOrganizerHomeDto.withoutEmptyAppDrawerFolders(): AiOrganizerHomeDto {
    val listedIds = appDrawerFolders.map { it.id }.toSet()
    val referencedFolderIds = appDrawer.mapNotNull { it.folderId }.filter { it in listedIds }.toSet()

    return copy(
        appDrawer = appDrawer.map { app ->
            val folderId = app.folderId

            if (folderId == null || folderId in referencedFolderIds) app else app.copy(folderId = null)
        },
        appDrawerFolders = appDrawerFolders.filter { it.id in referencedFolderIds },
    )
}

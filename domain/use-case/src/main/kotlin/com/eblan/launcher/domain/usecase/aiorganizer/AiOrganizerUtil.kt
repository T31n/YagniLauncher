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
import com.eblan.launcher.domain.model.folder.FolderEblanApplicationInfo
import com.eblan.launcher.domain.model.grid.Associate
import com.eblan.launcher.domain.model.grid.GridItem
import com.eblan.launcher.domain.model.grid.GridItemData
import com.eblan.launcher.domain.model.grid.GridItemSettings
import com.eblan.launcher.domain.model.grid.GridItems
import com.eblan.launcher.domain.model.userdata.EblanAction
import com.eblan.launcher.domain.model.userdata.EblanActionType
import com.eblan.launcher.domain.model.userdata.HomeSettings
import com.eblan.launcher.domain.repository.EblanApplicationInfoRepository
import com.eblan.launcher.domain.usecase.util.toGridItems
import java.io.File
import java.io.IOException
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

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

private fun GridItemData.parentFolderId(): String? = when (this) {
    is GridItemData.ApplicationInfo -> folderId
    is GridItemData.ShortcutInfo -> folderId
    is GridItemData.ShortcutConfig -> folderId
    is GridItemData.Folder -> folderId
    is GridItemData.Widget -> null
}

private fun GridItemData.withFolderId(folderId: String?): GridItemData = when (this) {
    is GridItemData.ApplicationInfo -> copy(folderId = folderId)
    is GridItemData.ShortcutInfo -> copy(folderId = folderId)
    is GridItemData.ShortcutConfig -> copy(folderId = folderId)
    is GridItemData.Folder -> copy(folderId = folderId)
    is GridItemData.Widget -> this
}

private fun AiGridItemType.matches(data: GridItemData): Boolean = when (data) {
    is GridItemData.ApplicationInfo -> this == AiGridItemType.Application
    is GridItemData.Widget -> this == AiGridItemType.Widget
    is GridItemData.ShortcutInfo -> this == AiGridItemType.Shortcut
    is GridItemData.Folder -> this == AiGridItemType.Folder
    is GridItemData.ShortcutConfig -> this == AiGridItemType.ShortcutConfig
}

private fun GridItem.position(context: AiOrganizerHomeContext): Int = startRow * context.columnsOf(associate) + startColumn

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

private fun GridItem.toAiGridItemDto(context: AiOrganizerHomeContext): AiGridItemDto {
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

/**
 * Validates app drawer folders and assignments. Only visible apps may be placed, and each at most once.
 */
internal fun AiOrganizerHomeDto.validateAppDrawer(context: AiOrganizerHomeContext) {
    require(appDrawerFolders.all { it.id.isNotBlank() && it.label.isNotBlank() }) {
        "App drawer folders require a non-blank id and label"
    }
    require(appDrawerFolders.map { it.id }.toSet().size == appDrawerFolders.size) {
        "Duplicate ids in appDrawerFolders"
    }

    val visibleApplicationKeys = context.apps.filter { !it.isHidden }.map { it.componentName to it.serialNumber }.toSet()
    val appDrawerApplicationKeys = appDrawer.map { it.componentName to it.serialNumber }

    require(appDrawerApplicationKeys.size == appDrawerApplicationKeys.toSet().size) {
        "Each app drawer application may appear only once"
    }
    require(appDrawerApplicationKeys.all { it in visibleApplicationKeys }) {
        "appDrawer references an unknown application"
    }

    val folderIds = appDrawerFolders.map { it.id }.toSet()

    require(appDrawer.all { it.folderId == null || it.folderId in folderIds }) {
        "appDrawer folderId must reference a listed app drawer folder"
    }
    require(appDrawerFolders.all { folder -> appDrawer.any { it.folderId == folder.id } }) {
        "Each app drawer folder must contain at least one application"
    }
}

private fun AiGridItemDto.appKey(): Pair<String, Long>? = if (componentName != null && serialNumber != null) {
    componentName to serialNumber
} else {
    null
}

/**
 * Validates a home document against the current home state and converts it into grid items.
 *
 * Items with an id from the current home keep that id and type. Every current non-widget item must be returned, so none is removed silently.
 * New items may only be applications or folders.
 * Widgets are fixed: they must keep their current page and position, and any omitted widget is kept in place.
 * Items that share a top-level cell are rejected, while folder children may overlap.
 *
 * @return The final grid items, with new ids assigned to new items.
 * @throws IllegalArgumentException when the document cannot be applied safely.
 */
@OptIn(ExperimentalUuidApi::class)
internal fun AiOrganizerHomeDto.validate(context: AiOrganizerHomeContext): List<GridItem> {
    // The page count is never taken from the response; the current home's page count is always used.
    val pageCount = context.pageCount

    // A missing dockGridItems array keeps the current dock untouched.
    val dockDtos = dockGridItems ?: context.gridItems
        .filter { it.associate == Associate.Dock }
        .map { it.toAiGridItemDto(context) }

    val surfaceItems = gridItems.map { it to Associate.Grid } + dockDtos.map { it to Associate.Dock }
    val responseItems = surfaceItems.map { it.first }

    require(responseItems.map { it.id }.toSet().size == responseItems.size) {
        "Duplicate ids in gridItems and dockGridItems"
    }

    surfaceItems.forEach { (item, surface) ->
        require(item.page in 0 until context.pageCountOf(surface, pageCount)) {
            "Invalid page ${item.page} for item ${item.id}"
        }
        require(item.position in 0 until context.columnsOf(surface) * context.rowsOf(surface)) {
            "Invalid position ${item.position} for item ${item.id}"
        }
    }

    val managedById = context.gridItems.associateBy { it.id }
    val responseIds = responseItems.map { it.id }.toSet()

    // Every current non-widget item must be returned, so apply never removes items silently. Widgets are always kept in place.
    val missingManagedIds = context.gridItems
        .filter { it.data !is GridItemData.Widget && it.id !in responseIds }
        .map { it.id }

    require(missingManagedIds.isEmpty()) {
        "Current items missing from the response: ${missingManagedIds.joinToString()}"
    }

    val responseById = responseItems.associateBy { it.id }
    val surfaceByItemId = surfaceItems.associate { (item, surface) -> item.id to surface }
    val parentIdById = responseItems.associate { it.id to it.folderId }

    surfaceItems.forEach { (item, surface) ->
        val managedGridItem = managedById[item.id]

        require(managedGridItem == null || managedGridItem.associate == surface) {
            "Item ${item.id} cannot move between grid and dock"
        }

        if (item.type == AiGridItemType.Widget) {
            require(item.folderId == null) { "Widget ${item.id} cannot be inside a folder" }
        }

        item.folderId?.let { parentId ->
            require(responseById[parentId]?.type == AiGridItemType.Folder) {
                "folderId of ${item.id} must reference a folder"
            }
            require(surfaceByItemId[parentId] == surface) {
                "folderId of ${item.id} must stay on the same surface"
            }
        }

        val visitedFolderIds = mutableSetOf(item.id)
        var ancestorId = item.folderId

        while (ancestorId != null) {
            require(visitedFolderIds.add(ancestorId)) { "Folder cycle involving ${item.id}" }

            ancestorId = parentIdById[ancestorId]
        }
    }

    surfaceItems.filter { it.first.type == AiGridItemType.Widget }.forEach { (item, _) ->
        val managedGridItem = managedById[item.id]

        require(managedGridItem != null && managedGridItem.data is GridItemData.Widget) {
            "Widget ${item.id} must be an existing widget"
        }
        require(item.page == managedGridItem.page && item.position == managedGridItem.position(context)) {
            "Widget ${item.id} is fixed and cannot be moved"
        }
    }

    val fixedWidgets = context.gridItems.filter { it.data is GridItemData.Widget }

    val finalIds = responseItems.associate { item ->
        val managedGridItem = managedById[item.id]

        if (managedGridItem != null) {
            require(item.type.matches(managedGridItem.data)) { "Type of item ${item.id} cannot be changed" }

            item.id to item.id
        } else {
            require(item.type == AiGridItemType.Application || item.type == AiGridItemType.Folder) {
                "New item ${item.id} must be an application or folder"
            }

            item.id to Uuid.random().toHexString()
        }
    }

    val childCounts = responseItems.mapNotNull { it.folderId }.groupingBy { it }.eachCount()

    responseItems.filter { it.type == AiGridItemType.Folder }.forEach { folder ->
        require(folder.id in childCounts) { "Folder ${folder.id} must contain at least one item" }
    }

    val finalItems = surfaceItems.filter { it.first.type != AiGridItemType.Widget }.map { (item, surface) ->
        val finalFolderId = item.folderId?.let { finalIds.getValue(it) }
        val managedGridItem = managedById[item.id]

        managedGridItem?.copy(
            page = item.page,
            startColumn = item.position % context.columnsOf(surface),
            startRow = item.position / context.columnsOf(surface),
            data = managedGridItem.data.withFolderId(finalFolderId),
        )
            ?: item.toNewGridItem(
                id = finalIds.getValue(item.id),
                folderId = finalFolderId,
                surface = surface,
                context = context,
            )
    } + fixedWidgets

    val finalApplicationKeys = finalItems.mapNotNull { item ->
        (item.data as? GridItemData.ApplicationInfo)?.let { it.componentName to it.serialNumber }
    }

    // Duplicates already present in the current home are preserved, so only newly added applications are checked.
    val newlyAddedApplicationKeys = finalItems
        .filter { it.id !in managedById }
        .mapNotNull { item -> (item.data as? GridItemData.ApplicationInfo)?.let { it.componentName to it.serialNumber } }

    require(newlyAddedApplicationKeys.all { key -> finalApplicationKeys.count { it == key } == 1 }) {
        "Each newly added application may appear only once"
    }

    val occupiedCells = mutableSetOf<Pair<Associate, Triple<Int, Int, Int>>>()

    finalItems.filter { it.data.parentFolderId() == null }.forEach { item ->
        require(item.startColumn + item.columnSpan <= context.columnsOf(item.associate)) {
            "Item ${item.id} exceeds the column count"
        }
        require(item.startRow + item.rowSpan <= context.rowsOf(item.associate)) {
            "Item ${item.id} exceeds the row count"
        }

        for (row in item.startRow until item.startRow + item.rowSpan) {
            for (column in item.startColumn until item.startColumn + item.columnSpan) {
                require(occupiedCells.add(item.associate to Triple(item.page, row, column))) {
                    "Conflicting placement at ${item.associate} page ${item.page}, row $row, column $column"
                }
            }
        }
    }

    validateAppDrawer(context)

    val tagIdentifiers = tags.map { it.id }

    require(tagIdentifiers.all { it > 0L }) { "Tag ids must be positive" }
    require(tagIdentifiers.size == tagIdentifiers.toSet().size) { "Duplicate tag ids" }
    require(tags.all { it.name.isNotBlank() }) { "Tag names must not be blank" }

    val installedApplicationKeys = context.apps.map { it.componentName to it.serialNumber }.toSet()
    val tagMembershipKeys = tagCrossRefs.map { Triple(it.componentName, it.serialNumber, it.id) }

    require(tagMembershipKeys.size == tagMembershipKeys.toSet().size) { "Duplicate tag assignment" }
    require(tagCrossRefs.all { it.componentName to it.serialNumber in installedApplicationKeys }) {
        "tagCrossRefs references an unknown application"
    }
    require(tagCrossRefs.all { it.id in tagIdentifiers }) { "tagCrossRefs references an unknown tag" }

    return finalItems
}

private fun AiGridItemDto.toNewGridItem(
    id: String,
    folderId: String?,
    surface: Associate,
    context: AiOrganizerHomeContext,
): GridItem {
    val emptyAction = EblanAction(
        eblanActionType = EblanActionType.None,
        serialNumber = 0L,
        componentName = "",
    )

    val data = when (type) {
        AiGridItemType.Application -> {
            val applicationKey = requireNotNull(appKey()) { "Application $id requires componentName and serialNumber" }
            val app = context.apps.firstOrNull {
                it.componentName == applicationKey.first && it.serialNumber == applicationKey.second
            }

            require(app != null && !app.isHidden) { "Application ${applicationKey.first} is not available" }

            GridItemData.ApplicationInfo(
                serialNumber = app.serialNumber,
                componentName = app.componentName,
                packageName = app.packageName,
                icon = app.icon,
                label = app.label,
                customIcon = null,
                customLabel = null,
                index = -1,
                folderId = folderId,
            )
        }

        AiGridItemType.Folder -> {
            require(!label.isNullOrBlank()) { "Folder $id requires a label" }

            GridItemData.Folder(
                label = label,
                icon = null,
                index = -1,
                folderId = folderId,
            )
        }

        else -> error("New item $id must be an application or folder")
    }

    return GridItem(
        id = id,
        page = page,
        startColumn = position % context.columnsOf(surface),
        startRow = position / context.columnsOf(surface),
        columnSpan = 1,
        rowSpan = 1,
        data = data,
        associate = surface,
        override = false,
        gridItemSettings = context.gridItemSettings,
        doubleTap = emptyAction,
        swipeUp = emptyAction,
        swipeDown = emptyAction,
    )
}

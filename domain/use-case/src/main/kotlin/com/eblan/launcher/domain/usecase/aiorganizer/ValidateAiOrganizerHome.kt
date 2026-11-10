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

import com.eblan.launcher.domain.model.grid.Associate
import com.eblan.launcher.domain.model.grid.GridItem
import com.eblan.launcher.domain.model.grid.GridItemData
import com.eblan.launcher.domain.model.userdata.EblanAction
import com.eblan.launcher.domain.model.userdata.EblanActionType
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

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
internal fun validateAiOrganizerHome(home: AiOrganizerHomeDto, context: AiOrganizerHomeContext): List<GridItem> {
    val surfaceItems = toSurfaceItems(home, context)
    val responseItems = surfaceItems.map { it.first }
    val managedById = context.gridItems.associateBy { it.id }

    requireUniqueResponseIds(responseItems)
    requireItemsWithinBounds(surfaceItems, context)
    requireCurrentItemsReturned(responseItems, context)
    requireValidItemPlacements(surfaceItems, responseItems, managedById)
    requireWidgetsFixed(surfaceItems, managedById, context)

    val finalIds = assignFinalIds(responseItems, managedById)

    requireFoldersNotEmpty(responseItems)

    val finalItems = buildFinalItems(surfaceItems, managedById, finalIds, context)

    requireNewApplicationsUnique(finalItems, managedById)
    requireNoOverlappingTopLevelItems(finalItems, context)
    validateAppDrawer(home, context)
    requireValidTags(home, context)

    return finalItems
}

private fun toSurfaceItems(
    home: AiOrganizerHomeDto,
    context: AiOrganizerHomeContext,
): List<Pair<AiGridItemDto, Associate>> {
    // A missing dockGridItems array keeps the current dock untouched.
    val dockDtos = home.dockGridItems ?: context.gridItems
        .filter { it.associate == Associate.Dock }
        .map { it.toAiGridItemDto(context) }

    return home.gridItems.map { it to Associate.Grid } + dockDtos.map { it to Associate.Dock }
}

private fun requireUniqueResponseIds(responseItems: List<AiGridItemDto>) {
    require(responseItems.map { it.id }.toSet().size == responseItems.size) {
        "Duplicate ids in gridItems and dockGridItems"
    }
}

private fun requireItemsWithinBounds(
    surfaceItems: List<Pair<AiGridItemDto, Associate>>,
    context: AiOrganizerHomeContext,
) {
    surfaceItems.forEach { (item, surface) ->
        require(item.page in 0 until context.pageCountOf(surface, context.pageCount)) {
            "Invalid page ${item.page} for item ${item.id}"
        }
        require(item.position in 0 until context.columnsOf(surface) * context.rowsOf(surface)) {
            "Invalid position ${item.position} for item ${item.id}"
        }
    }
}

// Every current non-widget item must be returned, so apply never removes items silently. Widgets are always kept in place.
private fun requireCurrentItemsReturned(responseItems: List<AiGridItemDto>, context: AiOrganizerHomeContext) {
    val responseIds = responseItems.map { it.id }.toSet()

    val missingManagedIds = context.gridItems
        .filter { it.data !is GridItemData.Widget && it.id !in responseIds }
        .map { it.id }

    require(missingManagedIds.isEmpty()) {
        "Current items missing from the response: ${missingManagedIds.joinToString()}"
    }
}

private fun requireValidItemPlacements(
    surfaceItems: List<Pair<AiGridItemDto, Associate>>,
    responseItems: List<AiGridItemDto>,
    managedById: Map<String, GridItem>,
) {
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
}

private fun requireWidgetsFixed(
    surfaceItems: List<Pair<AiGridItemDto, Associate>>,
    managedById: Map<String, GridItem>,
    context: AiOrganizerHomeContext,
) {
    surfaceItems.filter { it.first.type == AiGridItemType.Widget }.forEach { (item, _) ->
        val managedGridItem = managedById[item.id]

        require(managedGridItem != null && managedGridItem.data is GridItemData.Widget) {
            "Widget ${item.id} must be an existing widget"
        }
        require(item.page == managedGridItem.page && item.position == managedGridItem.position(context)) {
            "Widget ${item.id} is fixed and cannot be moved"
        }
    }
}

@OptIn(ExperimentalUuidApi::class)
private fun assignFinalIds(
    responseItems: List<AiGridItemDto>,
    managedById: Map<String, GridItem>,
): Map<String, String> = responseItems.associate { item ->
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

private fun requireFoldersNotEmpty(responseItems: List<AiGridItemDto>) {
    val childCounts = responseItems.mapNotNull { it.folderId }.groupingBy { it }.eachCount()

    responseItems.filter { it.type == AiGridItemType.Folder }.forEach { folder ->
        require(folder.id in childCounts) { "Folder ${folder.id} must contain at least one item" }
    }
}

private fun buildFinalItems(
    surfaceItems: List<Pair<AiGridItemDto, Associate>>,
    managedById: Map<String, GridItem>,
    finalIds: Map<String, String>,
    context: AiOrganizerHomeContext,
): List<GridItem> {
    val fixedWidgets = context.gridItems.filter { it.data is GridItemData.Widget }

    return surfaceItems.filter { it.first.type != AiGridItemType.Widget }.map { (item, surface) ->
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
}

private fun requireNewApplicationsUnique(finalItems: List<GridItem>, managedById: Map<String, GridItem>) {
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
}

private fun requireNoOverlappingTopLevelItems(finalItems: List<GridItem>, context: AiOrganizerHomeContext) {
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
}

/**
 * Validates app drawer folders and assignments. Only visible apps may be placed, and each at most once.
 */
private fun validateAppDrawer(home: AiOrganizerHomeDto, context: AiOrganizerHomeContext) {
    require(home.appDrawerFolders.all { it.id.isNotBlank() && it.label.isNotBlank() }) {
        "App drawer folders require a non-blank id and label"
    }
    require(home.appDrawerFolders.map { it.id }.toSet().size == home.appDrawerFolders.size) {
        "Duplicate ids in appDrawerFolders"
    }

    val visibleApplicationKeys = context.apps.filter { !it.isHidden }.map { it.componentName to it.serialNumber }.toSet()
    val appDrawerApplicationKeys = home.appDrawer.map { it.componentName to it.serialNumber }

    require(appDrawerApplicationKeys.size == appDrawerApplicationKeys.toSet().size) {
        "Each app drawer application may appear only once"
    }
    require(appDrawerApplicationKeys.all { it in visibleApplicationKeys }) {
        "appDrawer references an unknown application"
    }

    val folderIds = home.appDrawerFolders.map { it.id }.toSet()

    require(home.appDrawer.all { it.folderId == null || it.folderId in folderIds }) {
        "appDrawer folderId must reference a listed app drawer folder"
    }
    require(home.appDrawerFolders.all { folder -> home.appDrawer.any { it.folderId == folder.id } }) {
        "Each app drawer folder must contain at least one application"
    }
}

private fun requireValidTags(home: AiOrganizerHomeDto, context: AiOrganizerHomeContext) {
    val tagIdentifiers = home.tags.map { it.id }

    require(tagIdentifiers.all { it > 0L }) { "Tag ids must be positive" }
    require(tagIdentifiers.size == tagIdentifiers.toSet().size) { "Duplicate tag ids" }
    require(home.tags.all { it.name.isNotBlank() }) { "Tag names must not be blank" }

    val installedApplicationKeys = context.apps.map { it.componentName to it.serialNumber }.toSet()
    val tagMembershipKeys = home.tagCrossRefs.map { Triple(it.componentName, it.serialNumber, it.id) }

    require(tagMembershipKeys.size == tagMembershipKeys.toSet().size) { "Duplicate tag assignment" }
    require(home.tagCrossRefs.all { it.componentName to it.serialNumber in installedApplicationKeys }) {
        "tagCrossRefs references an unknown application"
    }
    require(home.tagCrossRefs.all { it.id in tagIdentifiers }) { "tagCrossRefs references an unknown tag" }
}

private fun AiGridItemDto.appKey(): Pair<String, Long>? = if (componentName != null && serialNumber != null) {
    componentName to serialNumber
} else {
    null
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

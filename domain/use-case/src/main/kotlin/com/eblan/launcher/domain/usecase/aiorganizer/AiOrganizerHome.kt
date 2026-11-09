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
import com.eblan.launcher.domain.model.userdata.EblanAction
import com.eblan.launcher.domain.model.userdata.EblanActionType
import com.eblan.launcher.domain.model.userdata.HomeSettings
import com.eblan.launcher.domain.repository.EblanApplicationInfoRepository
import com.eblan.launcher.domain.usecase.util.toGridItems
import java.io.File
import java.io.IOException
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

internal const val MAX_AI_PAGE_COUNT = 100

/** Writes via a temp file and rename so a reader never sees a partially written home file. */
internal fun writeHomeFile(directory: File, name: String, content: ByteArray) {
    val target = File(directory, name)
    val temp = File(directory, "$name.tmp")

    try {
        temp.writeBytes(content)

        if (!temp.renameTo(target)) {
            throw IOException("Unable to move ${temp.name} to ${target.name}")
        }
    } finally {
        temp.delete()
    }
}

/**
 * Describes the current home state that an AI organization must be consistent with.
 *
 * @property columns Columns per page.
 * @property rows Rows per page.
 * @property gridItemSettings Settings applied to newly created grid items.
 * @property gridItems Grid items associated with the home screen, including folder children.
 * @property apps All installed applications, including hidden ones that may still be tagged.
 */
internal data class AiOrganizerHomeContext(
    val columns: Int,
    val rows: Int,
    val gridItemSettings: GridItemSettings,
    val gridItems: List<GridItem>,
    val apps: List<EblanApplicationInfo>,
)

internal fun buildAiOrganizerHomeContext(
    homeSettings: HomeSettings,
    gridItems: GridItems,
    apps: List<EblanApplicationInfo>,
): AiOrganizerHomeContext = AiOrganizerHomeContext(
    columns = homeSettings.columns,
    rows = homeSettings.rows,
    gridItemSettings = homeSettings.gridItemSettings,
    gridItems = gridItems.toGridItems().filter { it.associate == Associate.Grid },
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

private fun GridItem.position(columns: Int): Int = startRow * columns + startColumn

private fun EblanApplicationInfo.toAiAppDto(): AiAppDto = AiAppDto(
    componentName = componentName,
    packageName = packageName,
    serialNumber = serialNumber,
    label = label,
)

internal fun AiOrganizerHomeContext.toRequestDto(pageCount: Int): AiOrganizerRequestDto = AiOrganizerRequestDto(
    gridItems = gridItems.map { item ->
        val fields = item.data.toAiItemFields()

        AiRequestGridItemDto(
            id = item.id,
            type = fields.type,
            componentName = fields.componentName,
            packageName = fields.packageName,
            serialNumber = fields.serialNumber,
            label = fields.label,
            page = item.page,
            position = item.position(columns),
            columnSpan = item.columnSpan,
            rowSpan = item.rowSpan,
            folderId = item.data.parentFolderId(),
        )
    },
    appDrawer = apps.filter { !it.isHidden }.map { it.toAiAppDto() },
    settings = AiRequestSettingsDto(
        pageCount = pageCount,
        columns = columns,
        rows = rows,
    ),
)

/**
 * Serializes the current home so it can be applied later with the same validation as a generated
 * organization. The app drawer is omitted because it is derived from the device on apply.
 */
internal fun AiOrganizerHomeContext.toSnapshotDto(
    pageCount: Int,
    tags: List<EblanApplicationInfoTag>,
    tagCrossRefs: List<EblanApplicationInfoTagCrossRef>,
): AiOrganizerHomeDto = AiOrganizerHomeDto(
    gridItems = gridItems.map { item ->
        val fields = item.data.toAiItemFields()

        AiGridItemDto(
            id = item.id,
            type = fields.type,
            componentName = fields.componentName,
            serialNumber = fields.serialNumber,
            label = fields.label,
            page = item.page,
            position = item.position(columns),
            folderId = item.data.parentFolderId(),
        )
    },
    tags = tags.map { AiTagDto(id = it.id, name = it.name, index = it.index) },
    tagCrossRefs = tagCrossRefs.map {
        AiTagCrossRefDto(componentName = it.componentName, serialNumber = it.serialNumber, id = it.id)
    },
    settings = AiHomeSettingsDto(pageCount = pageCount),
)

/**
 * Extracts the JSON document from a model response, tolerating surrounding Markdown code fences.
 */
internal fun parseAiOrganizerHome(raw: String): AiOrganizerHomeDto {
    val trimmed = raw.trim()
        .removePrefix("```json")
        .removePrefix("```")
        .removeSuffix("```")
        .trim()

    val start = trimmed.indexOf('{')
    val end = trimmed.lastIndexOf('}')

    require(start in 0..<end) { "The AI response does not contain a JSON object" }

    return aiOrganizerJson.decodeFromString<AiOrganizerHomeDto>(trimmed.substring(start, end + 1))
}

/**
 * Drops items that cannot be restored, such as widgets and shortcuts whose bindings were released,
 * and any folder or child left without a valid parent or children.
 */
internal fun AiOrganizerHomeDto.withoutUnrecoverableItems(context: AiOrganizerHomeContext): AiOrganizerHomeDto {
    val managedIds = context.gridItems.map { it.id }.toSet()
    val installedAppKeys = context.apps
        .filter { !it.isHidden }
        .map { it.componentName to it.serialNumber }
        .toSet()

    var kept = gridItems.filter { item ->
        item.id in managedIds || when (item.type) {
            AiGridItemType.Application -> item.appKey()?.let { it in installedAppKeys } == true
            AiGridItemType.Folder -> true
            else -> false
        }
    }

    while (true) {
        val keptIds = kept.map { it.id }.toSet()
        val parentFolderIds = kept.mapNotNull { it.folderId }.toSet()

        val next = kept.filter { item ->
            val parentKept = item.folderId == null || item.folderId in keptIds
            val folderHasChildren = item.type != AiGridItemType.Folder || item.id in parentFolderIds

            parentKept && folderHasChildren
        }

        if (next.size == kept.size) break

        kept = next
    }

    return copy(gridItems = kept)
}

private fun AiGridItemDto.appKey(): Pair<String, Long>? = if (componentName != null && serialNumber != null) {
    componentName to serialNumber
} else {
    null
}

/**
 * Validates a home document against the current home state and converts it into grid items.
 *
 * Items with an id from the current home keep that id and type. New items may only be applications or folders.
 * Widgets are fixed: they must keep their current page and position, and any omitted widget is kept in place.
 * Items that share a top-level cell are rejected, while folder children may overlap.
 *
 * @return The final grid items, with new ids assigned to new items.
 * @throws IllegalArgumentException when the document cannot be applied safely.
 */
@OptIn(ExperimentalUuidApi::class)
internal fun AiOrganizerHomeDto.validate(context: AiOrganizerHomeContext): List<GridItem> {
    val pageCount = settings.pageCount
    val columns = context.columns
    val rows = context.rows

    require(pageCount in 1..MAX_AI_PAGE_COUNT) {
        "pageCount must be between 1 and $MAX_AI_PAGE_COUNT"
    }

    require(gridItems.map { it.id }.toSet().size == gridItems.size) { "Duplicate ids in gridItems" }

    val totalPositions = columns * rows

    gridItems.forEach {
        require(it.page in 0 until pageCount) { "Invalid page ${it.page} for item ${it.id}" }
        require(it.position in 0 until totalPositions) { "Invalid position ${it.position} for item ${it.id}" }
    }

    val managedById = context.gridItems.associateBy { it.id }
    val responseById = gridItems.associateBy { it.id }

    gridItems.forEach { item ->
        if (item.type == AiGridItemType.Widget || item.type == AiGridItemType.Folder) {
            require(item.folderId == null) { "${item.type} ${item.id} cannot be inside a folder" }
        }

        item.folderId?.let { parentId ->
            require(responseById[parentId]?.type == AiGridItemType.Folder) {
                "folderId of ${item.id} must reference a folder"
            }
        }
    }

    gridItems.filter { it.type == AiGridItemType.Widget }.forEach { item ->
        val managed = managedById[item.id]

        require(managed != null && managed.data is GridItemData.Widget) {
            "Widget ${item.id} must be an existing widget"
        }
        require(item.page == managed.page && item.position == managed.position(columns)) {
            "Widget ${item.id} is fixed and cannot be moved"
        }
    }

    val fixedWidgets = context.gridItems.filter { it.data is GridItemData.Widget }

    val finalIds = gridItems.associate { item ->
        val managed = managedById[item.id]

        if (managed != null) {
            require(item.type.matches(managed.data)) { "Type of item ${item.id} cannot be changed" }

            item.id to item.id
        } else {
            require(item.type == AiGridItemType.Application || item.type == AiGridItemType.Folder) {
                "New item ${item.id} must be an application or folder"
            }

            item.id to Uuid.random().toHexString()
        }
    }

    val childCounts = gridItems.mapNotNull { it.folderId }.groupingBy { it }.eachCount()

    gridItems.filter { it.type == AiGridItemType.Folder }.forEach { folder ->
        require(folder.id in childCounts) { "Folder ${folder.id} must contain at least one item" }
    }

    val finalItems = gridItems.filter { it.type != AiGridItemType.Widget }.map { item ->
        val finalFolderId = item.folderId?.let { finalIds.getValue(it) }
        val managed = managedById[item.id]

        managed?.copy(
            page = item.page,
            startColumn = item.position % columns,
            startRow = item.position / columns,
            data = managed.data.withFolderId(finalFolderId),
        )
            ?: item.toNewGridItem(
                id = finalIds.getValue(item.id),
                folderId = finalFolderId,
                context = context,
            )
    } + fixedWidgets

    val applicationKeys = finalItems.mapNotNull { item ->
        (item.data as? GridItemData.ApplicationInfo)?.let { it.componentName to it.serialNumber }
    }

    require(applicationKeys.size == applicationKeys.toSet().size) { "Each application may appear only once" }

    val occupiedCells = mutableSetOf<Triple<Int, Int, Int>>()

    finalItems.filter { it.data.parentFolderId() == null }.forEach { item ->
        require(item.startColumn + item.columnSpan <= columns) { "Item ${item.id} exceeds the column count" }
        require(item.startRow + item.rowSpan <= rows) { "Item ${item.id} exceeds the row count" }

        for (row in item.startRow until item.startRow + item.rowSpan) {
            for (column in item.startColumn until item.startColumn + item.columnSpan) {
                require(occupiedCells.add(Triple(item.page, row, column))) {
                    "Conflicting placement at page ${item.page}, row $row, column $column"
                }
            }
        }
    }

    val tagIds = tags.map { it.id }

    require(tagIds.all { it > 0L }) { "Tag ids must be positive" }
    require(tagIds.size == tagIds.toSet().size) { "Duplicate tag ids" }
    require(tags.all { it.name.isNotBlank() }) { "Tag names must not be blank" }

    val appKeys = context.apps.map { it.componentName to it.serialNumber }.toSet()
    val membershipKeys = tagCrossRefs.map { Triple(it.componentName, it.serialNumber, it.id) }

    require(membershipKeys.size == membershipKeys.toSet().size) { "Duplicate tag assignment" }
    require(tagCrossRefs.all { it.componentName to it.serialNumber in appKeys }) {
        "tagCrossRefs references an unknown application"
    }
    require(tagCrossRefs.all { it.id in tagIds }) { "tagCrossRefs references an unknown tag" }

    return finalItems
}

private fun AiGridItemDto.toNewGridItem(
    id: String,
    folderId: String?,
    context: AiOrganizerHomeContext,
): GridItem {
    val emptyAction = EblanAction(
        eblanActionType = EblanActionType.None,
        serialNumber = 0L,
        componentName = "",
    )

    val data = when (type) {
        AiGridItemType.Application -> {
            val key = requireNotNull(appKey()) { "Application $id requires componentName and serialNumber" }
            val app = context.apps.firstOrNull { it.componentName == key.first && it.serialNumber == key.second }

            require(app != null && !app.isHidden) { "Application ${key.first} is not available" }

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
        startColumn = position % context.columns,
        startRow = position / context.columns,
        columnSpan = 1,
        rowSpan = 1,
        data = data,
        associate = Associate.Grid,
        override = false,
        gridItemSettings = context.gridItemSettings,
        doubleTap = emptyAction,
        swipeUp = emptyAction,
        swipeDown = emptyAction,
    )
}

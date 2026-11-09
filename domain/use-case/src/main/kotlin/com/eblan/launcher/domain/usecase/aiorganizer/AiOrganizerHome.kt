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
import com.eblan.launcher.domain.model.grid.ApplicationInfoGridItem
import com.eblan.launcher.domain.model.grid.Associate
import com.eblan.launcher.domain.model.grid.GridItems

internal const val MAX_AI_PAGE_COUNT = 100

/**
 * Describes the current home state that an AI organization must be consistent with.
 *
 * @property columns Columns per page.
 * @property rows Rows per page.
 * @property organizableItems Top-level single-cell application items that the AI may place.
 * @property occupiedPositions Cell positions occupied by fixed items, keyed by page.
 * @property apps All installed applications, including hidden ones that may still be tagged.
 */
internal data class AiOrganizerHomeContext(
    val columns: Int,
    val rows: Int,
    val organizableItems: List<ApplicationInfoGridItem>,
    val occupiedPositions: Map<Int, Set<Int>>,
    val apps: List<EblanApplicationInfo>,
) {
    val minPageCount: Int = maxOf(1, (occupiedPositions.keys.maxOrNull() ?: -1) + 1)
}

internal fun ApplicationInfoGridItem.isOrganizable(): Boolean = associate == Associate.Grid &&
    folderId == null &&
    columnSpan == 1 &&
    rowSpan == 1

internal fun buildAiOrganizerHomeContext(
    columns: Int,
    rows: Int,
    gridItems: GridItems,
    apps: List<EblanApplicationInfo>,
): AiOrganizerHomeContext {
    val appGridItemCounts = gridItems.applicationInfoGridItems
        .groupingBy { it.componentName }
        .eachCount()

    // Items sharing a component name cannot be addressed unambiguously, so they stay fixed.
    val organizableItems = gridItems.applicationInfoGridItems.filter {
        it.isOrganizable() && appGridItemCounts[it.componentName] == 1
    }
    val organizableIds = organizableItems.map { it.id }.toSet()

    val footprints = buildList {
        gridItems.applicationInfoGridItems
            .filter { it.id !in organizableIds && it.associate == Associate.Grid && it.folderId == null }
            .forEach { add(Footprint(it.page, it.startColumn, it.startRow, it.columnSpan, it.rowSpan)) }
        gridItems.widgetGridItems
            .filter { it.associate == Associate.Grid }
            .forEach { add(Footprint(it.page, it.startColumn, it.startRow, it.columnSpan, it.rowSpan)) }
        gridItems.shortcutInfoGridItems
            .filter { it.associate == Associate.Grid && it.folderId == null }
            .forEach { add(Footprint(it.page, it.startColumn, it.startRow, it.columnSpan, it.rowSpan)) }
        gridItems.shortcutConfigGridItems
            .filter { it.associate == Associate.Grid && it.folderId == null }
            .forEach { add(Footprint(it.page, it.startColumn, it.startRow, it.columnSpan, it.rowSpan)) }
        gridItems.folderGridItems
            .filter { it.associate == Associate.Grid && it.folderId == null }
            .forEach { add(Footprint(it.page, it.startColumn, it.startRow, it.columnSpan, it.rowSpan)) }
    }

    val occupiedPositions = mutableMapOf<Int, MutableSet<Int>>()

    footprints.forEach { footprint ->
        for (row in footprint.startRow until footprint.startRow + footprint.rowSpan) {
            for (column in footprint.startColumn until footprint.startColumn + footprint.columnSpan) {
                if (row in 0 until rows && column in 0 until columns) {
                    occupiedPositions.getOrPut(footprint.page) { mutableSetOf() }.add(row * columns + column)
                }
            }
        }
    }

    return AiOrganizerHomeContext(
        columns = columns,
        rows = rows,
        organizableItems = organizableItems,
        occupiedPositions = occupiedPositions,
        apps = apps,
    )
}

private data class Footprint(
    val page: Int,
    val startColumn: Int,
    val startRow: Int,
    val columnSpan: Int,
    val rowSpan: Int,
)

internal fun ApplicationInfoGridItem.position(columns: Int): Int = startRow * columns + startColumn

internal fun AiOrganizerHomeContext.toRequestDto(pageCount: Int): AiOrganizerRequestDto = AiOrganizerRequestDto(
    gridItems = organizableItems.map {
        AiGridItemDto(
            componentName = it.componentName,
            packageName = it.packageName,
            label = it.label,
            page = it.page,
            position = it.position(columns),
        )
    },
    appDrawer = apps.filter { !it.isHidden }.map {
        AiAppDto(
            componentName = it.componentName,
            packageName = it.packageName,
            serialNumber = it.serialNumber,
            label = it.label,
        )
    },
    settings = AiRequestSettingsDto(
        pageCount = pageCount,
        columns = columns,
        rows = rows,
    ),
)

/**
 * Builds the same document format as the AI response from the current home state.
 */
internal fun AiOrganizerHomeContext.toSnapshotDto(
    pageCount: Int,
    tags: List<EblanApplicationInfoTag>,
    tagCrossRefs: List<EblanApplicationInfoTagCrossRef>,
): AiOrganizerHomeDto = AiOrganizerHomeDto(
    gridItems = organizableItems.map {
        AiGridItemDto(
            componentName = it.componentName,
            packageName = it.packageName,
            label = it.label,
            page = it.page,
            position = it.position(columns),
        )
    },
    appDrawer = apps.map {
        AiAppDto(
            componentName = it.componentName,
            packageName = it.packageName,
            serialNumber = it.serialNumber,
            label = it.label,
        )
    },
    tags = tags.map { AiTagDto(id = it.id, name = it.name, index = it.index) },
    tagCrossRefs = tagCrossRefs.map {
        AiTagCrossRefDto(
            componentName = it.componentName,
            serialNumber = it.serialNumber,
            id = it.id,
        )
    },
    settings = AiHomeSettingsDto(pageCount = pageCount),
)

/**
 * Extracts the JSON document from a model response, tolerating surrounding markdown code fences.
 */
internal fun parseAiOrganizerHome(raw: String): AiOrganizerHomeDto {
    val trimmed = raw.trim()
        .removePrefix("```json")
        .removePrefix("```")
        .removeSuffix("```")
        .trim()

    val start = trimmed.indexOf('{')
    val end = trimmed.lastIndexOf('}')

    require(start >= 0 && end > start) { "The AI response does not contain a JSON object" }

    return aiOrganizerJson.decodeFromString<AiOrganizerHomeDto>(trimmed.substring(start, end + 1))
}

/**
 * Validates a home document against the current home state.
 *
 * @throws IllegalArgumentException when the document cannot be applied safely.
 */
internal fun AiOrganizerHomeDto.validate(context: AiOrganizerHomeContext) {
    val pageCount = settings.pageCount

    require(pageCount in context.minPageCount..MAX_AI_PAGE_COUNT) {
        "pageCount must be between ${context.minPageCount} and $MAX_AI_PAGE_COUNT"
    }

    val organizableNames = context.organizableItems.map { it.componentName }.toSet()
    val placedNames = gridItems.map { it.componentName }

    require(placedNames.size == placedNames.toSet().size) { "Duplicate home items in gridItems" }
    require(placedNames.all { it in organizableNames }) { "gridItems references an unknown application" }
    require(placedNames.toSet() == organizableNames) { "gridItems must place every home application exactly once" }

    val totalPositions = context.columns * context.rows
    val usedPositions = mutableSetOf<Pair<Int, Int>>()

    gridItems.forEach {
        require(it.page in 0 until pageCount) { "Invalid page ${it.page} for ${it.componentName}" }
        require(it.position in 0 until totalPositions) { "Invalid position ${it.position} for ${it.componentName}" }
        require(it.position !in context.occupiedPositions[it.page].orEmpty()) {
            "Position ${it.position} on page ${it.page} is occupied by another item"
        }
        require(usedPositions.add(it.page to it.position)) {
            "Conflicting placement at page ${it.page}, position ${it.position}"
        }
    }

    val appKeys = context.apps.map { it.componentName to it.serialNumber }.toSet()
    val drawerKeys = appDrawer.map { it.componentName to it.serialNumber }

    require(drawerKeys.size == drawerKeys.toSet().size) { "Duplicate applications in appDrawer" }
    require(drawerKeys.all { it in appKeys }) { "appDrawer references an unknown application" }

    val tagIds = tags.map { it.id }

    require(tagIds.all { it > 0L }) { "Tag ids must be positive" }
    require(tagIds.size == tagIds.toSet().size) { "Duplicate tag ids" }
    require(tags.all { it.name.isNotBlank() }) { "Tag names must not be blank" }

    val crossRefKeys = tagCrossRefs.map { it.componentName to it.serialNumber }

    require(crossRefKeys.size == crossRefKeys.toSet().size) { "Application assigned to a tag more than once" }
    require(crossRefKeys.all { it in appKeys }) { "tagCrossRefs references an unknown application" }
    require(tagCrossRefs.all { it.id in tagIds }) { "tagCrossRefs references an unknown tag" }
}

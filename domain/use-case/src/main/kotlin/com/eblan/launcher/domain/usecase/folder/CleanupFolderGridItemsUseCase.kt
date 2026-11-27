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

import com.eblan.launcher.domain.common.Dispatcher
import com.eblan.launcher.domain.common.EblanDispatchers
import com.eblan.launcher.domain.model.grid.GridItem
import com.eblan.launcher.domain.model.grid.GridItemData
import com.eblan.launcher.domain.repository.GridRepository
import com.eblan.launcher.domain.usecase.util.toGridItems
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import javax.inject.Inject

/** Removes empty grid folders and collapses single-item folders into their remaining child. */
class CleanupFolderGridItemsUseCase @Inject constructor(
    private val gridRepository: GridRepository,
    @param:Dispatcher(EblanDispatchers.Default) private val defaultDispatcher: CoroutineDispatcher,
) {
    /**
     * Deletes any empty folders and replaces any folder containing a single child with that child
     * at the folder's position, resolving nested folders recursively.
     */
    suspend operator fun invoke() = withContext(defaultDispatcher) {
        gridRepository.withTransaction {
            val gridItems = gridRepository.getGridItems().toGridItems()

            val gridItemsByFolderId = gridItems
                .groupBy { currentGridItem -> currentGridItem.folderIdOrNull() }
                .mapValues { (_, currentGridItems) -> currentGridItems.toMutableList() }
                .toMutableMap()

            val gridItemsToUpsertById = mutableMapOf<String, GridItem>()
            val gridItemsToDeleteById = mutableMapOf<String, GridItem>()
            val visitedFolderGridItemIds = mutableSetOf<String>()

            gridItems
                .filter { currentGridItem -> currentGridItem.data is GridItemData.Folder }
                .forEach { folderGridItem ->
                    cleanupFolderGridItem(
                        folderGridItem = folderGridItem,
                        gridItemsByFolderId = gridItemsByFolderId,
                        gridItemsToUpsertById = gridItemsToUpsertById,
                        gridItemsToDeleteById = gridItemsToDeleteById,
                        visitedFolderGridItemIds = visitedFolderGridItemIds,
                    )
                }

            if (gridItemsToUpsertById.isNotEmpty()) {
                gridRepository.upsertGridItems(gridItemsToUpsertById.values.toList())
            }

            if (gridItemsToDeleteById.isNotEmpty()) {
                gridRepository.deleteGridItems(gridItemsToDeleteById.values.toList())
            }
        }
    }

    private fun cleanupFolderGridItem(
        folderGridItem: GridItem,
        gridItemsByFolderId: MutableMap<String?, MutableList<GridItem>>,
        gridItemsToUpsertById: MutableMap<String, GridItem>,
        gridItemsToDeleteById: MutableMap<String, GridItem>,
        visitedFolderGridItemIds: MutableSet<String>,
    ) {
        val folderGridItemData = folderGridItem.data as? GridItemData.Folder ?: return
        if (!visitedFolderGridItemIds.add(folderGridItem.id)) return

        gridItemsByFolderId[folderGridItem.id]
            ?.toList()
            .orEmpty()
            .filter { currentGridItem -> currentGridItem.data is GridItemData.Folder }
            .forEach { childFolderGridItem ->
                cleanupFolderGridItem(
                    folderGridItem = childFolderGridItem,
                    gridItemsByFolderId = gridItemsByFolderId,
                    gridItemsToUpsertById = gridItemsToUpsertById,
                    gridItemsToDeleteById = gridItemsToDeleteById,
                    visitedFolderGridItemIds = visitedFolderGridItemIds,
                )
            }

        val childGridItems = gridItemsByFolderId[folderGridItem.id].orEmpty()
        if (childGridItems.size > 1) return

        gridItemsByFolderId[folderGridItemData.folderId]?.removeAll { currentGridItem ->
            currentGridItem.id == folderGridItem.id
        }

        val singleChildGridItem = childGridItems.singleOrNull()
        if (singleChildGridItem != null && singleChildGridItem.id != folderGridItem.id) {
            val updatedChildGridItem = singleChildGridItem.moveToFolderGridItemPosition(
                folderGridItem = folderGridItem,
                folderGridItemData = folderGridItemData,
            )

            gridItemsToUpsertById[updatedChildGridItem.id] = updatedChildGridItem
            gridItemsByFolderId.getOrPut(folderGridItemData.folderId) { mutableListOf() }
                .add(updatedChildGridItem)
        }

        gridItemsByFolderId.remove(folderGridItem.id)
        gridItemsToUpsertById.remove(folderGridItem.id)
        gridItemsToDeleteById[folderGridItem.id] = folderGridItem
    }

    private fun GridItem.folderIdOrNull(): String? = when (val gridItemData = data) {
        is GridItemData.ApplicationInfo -> gridItemData.folderId
        is GridItemData.Folder -> gridItemData.folderId
        is GridItemData.ShortcutConfig -> gridItemData.folderId
        is GridItemData.ShortcutInfo -> gridItemData.folderId
        is GridItemData.Widget -> null
    }

    private fun GridItem.moveToFolderGridItemPosition(
        folderGridItem: GridItem,
        folderGridItemData: GridItemData.Folder,
    ): GridItem {
        val targetFolderId = folderGridItemData.folderId
        val targetGridItemIndex = if (targetFolderId == null) -1 else folderGridItemData.index

        val updatedGridItemData = when (val childGridItemData = data) {
            is GridItemData.ApplicationInfo -> childGridItemData.copy(
                index = targetGridItemIndex,
                folderId = targetFolderId,
            )

            is GridItemData.ShortcutConfig -> childGridItemData.copy(
                index = targetGridItemIndex,
                folderId = targetFolderId,
            )

            is GridItemData.ShortcutInfo -> childGridItemData.copy(
                index = targetGridItemIndex,
                folderId = targetFolderId,
            )

            is GridItemData.Folder -> childGridItemData.copy(
                index = targetGridItemIndex,
                folderId = targetFolderId,
            )

            is GridItemData.Widget -> childGridItemData
        }

        return if (targetFolderId == null) {
            copy(
                page = folderGridItem.page,
                startColumn = folderGridItem.startColumn,
                startRow = folderGridItem.startRow,
                columnSpan = folderGridItem.columnSpan,
                rowSpan = folderGridItem.rowSpan,
                associate = folderGridItem.associate,
                data = updatedGridItemData,
            )
        } else {
            copy(
                page = folderGridItem.page,
                startColumn = -1,
                startRow = -1,
                columnSpan = 1,
                rowSpan = 1,
                associate = folderGridItem.associate,
                data = updatedGridItemData,
            )
        }
    }
}

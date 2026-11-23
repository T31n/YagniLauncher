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
        val gridItems = gridRepository.getGridItems().toGridItems()

        val childrenByFolderId = gridItems
            .groupBy { currentGridItem -> currentGridItem.folderIdOrNull() }
            .mapValues { (_, currentGridItems) -> currentGridItems.toMutableList() }
            .toMutableMap()

        val itemsToUpsertById = mutableMapOf<String, GridItem>()
        val itemsToDeleteById = mutableMapOf<String, GridItem>()
        val visitedFolderIds = mutableSetOf<String>()

        fun cleanupFolder(folderGridItem: GridItem) {
            val folderData = folderGridItem.data as? GridItemData.Folder ?: return
            if (!visitedFolderIds.add(folderGridItem.id)) return

            childrenByFolderId[folderGridItem.id]
                ?.toList()
                .orEmpty()
                .filter { currentGridItem -> currentGridItem.data is GridItemData.Folder }
                .forEach(::cleanupFolder)

            val children = childrenByFolderId[folderGridItem.id].orEmpty()
            if (children.size > 1) return

            childrenByFolderId[folderData.folderId]?.removeAll { currentGridItem ->
                currentGridItem.id == folderGridItem.id
            }

            val singleChild = children.singleOrNull()
            if (singleChild != null && singleChild.id != folderGridItem.id) {
                val updatedChild = singleChild.moveToFolderPosition(
                    folderGridItem = folderGridItem,
                    folderData = folderData,
                )

                itemsToUpsertById[updatedChild.id] = updatedChild
                childrenByFolderId.getOrPut(folderData.folderId) { mutableListOf() }.add(updatedChild)
            }

            childrenByFolderId.remove(folderGridItem.id)
            itemsToUpsertById.remove(folderGridItem.id)
            itemsToDeleteById[folderGridItem.id] = folderGridItem
        }

        gridItems
            .filter { currentGridItem -> currentGridItem.data is GridItemData.Folder }
            .forEach(::cleanupFolder)

        if (itemsToUpsertById.isNotEmpty()) {
            gridRepository.upsertGridItems(gridItems = itemsToUpsertById.values.toList())
        }

        if (itemsToDeleteById.isNotEmpty()) {
            gridRepository.deleteGridItems(gridItems = itemsToDeleteById.values.toList())
        }
    }

    private fun GridItem.folderIdOrNull(): String? = when (val itemData = data) {
        is GridItemData.ApplicationInfo -> itemData.folderId
        is GridItemData.Folder -> itemData.folderId
        is GridItemData.ShortcutConfig -> itemData.folderId
        is GridItemData.ShortcutInfo -> itemData.folderId
        is GridItemData.Widget -> null
    }

    private fun GridItem.moveToFolderPosition(
        folderGridItem: GridItem,
        folderData: GridItemData.Folder,
    ): GridItem {
        val targetFolderId = folderData.folderId
        val targetIndex = if (targetFolderId == null) -1 else folderData.index

        val updatedData = when (val childData = data) {
            is GridItemData.ApplicationInfo -> childData.copy(
                index = targetIndex,
                folderId = targetFolderId,
            )

            is GridItemData.ShortcutConfig -> childData.copy(
                index = targetIndex,
                folderId = targetFolderId,
            )

            is GridItemData.ShortcutInfo -> childData.copy(
                index = targetIndex,
                folderId = targetFolderId,
            )

            is GridItemData.Folder -> childData.copy(
                index = targetIndex,
                folderId = targetFolderId,
            )

            is GridItemData.Widget -> childData
        }

        return if (targetFolderId == null) {
            copy(
                page = folderGridItem.page,
                startColumn = folderGridItem.startColumn,
                startRow = folderGridItem.startRow,
                columnSpan = folderGridItem.columnSpan,
                rowSpan = folderGridItem.rowSpan,
                associate = folderGridItem.associate,
                data = updatedData,
            )
        } else {
            copy(
                page = folderGridItem.page,
                startColumn = -1,
                startRow = -1,
                columnSpan = 1,
                rowSpan = 1,
                associate = folderGridItem.associate,
                data = updatedData,
            )
        }
    }
}

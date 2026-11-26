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
import com.eblan.launcher.domain.model.folder.FolderEblanApplicationInfoGridItem
import com.eblan.launcher.domain.model.folder.FolderEblanApplicationInfoGridItemData
import com.eblan.launcher.domain.model.grid.MoveFolderEblanApplicationInfoGridItemResult
import com.eblan.launcher.domain.repository.FolderGridItemTransaction
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import javax.inject.Inject

/** Reorders application-folder items and persists their updated child indices. */
class MoveFolderEblanApplicationInfoGridItemUseCase @Inject constructor(
    private val folderGridItemTransaction: FolderGridItemTransaction,
    @param:Dispatcher(EblanDispatchers.Default) private val defaultDispatcher: CoroutineDispatcher,
) {
    /**
     * Moves a folder child to the index indicated by its drag position on the current page.
     *
     * @param columns The number of columns in the folder popup grid.
     * @param rows The number of rows in the folder popup grid.
     * @param folderEblanApplicationInfoGridItems The ordered child items in the folder popup.
     * @param folderEblanApplicationInfoGridItem The child item being moved.
     * @param dragX The horizontal drag coordinate in pixels.
     * @param dragY The vertical drag coordinate in pixels.
     * @param gridWidth The popup grid width in pixels.
     * @param gridHeight The popup grid height in pixels.
     * @param currentPage The page containing the drop position.
     * @return A successful move result for the moved child.
     */
    suspend operator fun invoke(
        columns: Int,
        rows: Int,
        folderEblanApplicationInfoGridItems: List<FolderEblanApplicationInfoGridItem>,
        folderEblanApplicationInfoGridItem: FolderEblanApplicationInfoGridItem,
        dragX: Int,
        dragY: Int,
        gridWidth: Int,
        gridHeight: Int,
        currentPage: Int,
    ): MoveFolderEblanApplicationInfoGridItemResult = withContext(defaultDispatcher) {
        val gridItemsPerPage = columns * rows

        val cellWidth = gridWidth / columns
        val cellHeight = gridHeight / rows

        val targetColumn = dragX / cellWidth
        val targetRow = dragY / cellHeight

        val targetIndex =
            currentPage * gridItemsPerPage + targetRow * columns + targetColumn

        val updatedFolderEblanApplicationInfoGridItems =
            folderEblanApplicationInfoGridItems.toMutableList()

        val movingIndex =
            updatedFolderEblanApplicationInfoGridItems.indexOfFirst { currentFolderEblanApplicationInfoGridItem ->
                currentFolderEblanApplicationInfoGridItem.id == folderEblanApplicationInfoGridItem.id
            }

        if (movingIndex != -1) {
            updatedFolderEblanApplicationInfoGridItems.add(
                targetIndex.coerceIn(
                    0,
                    updatedFolderEblanApplicationInfoGridItems.size - 1,
                ),
                updatedFolderEblanApplicationInfoGridItems.removeAt(movingIndex),
            )
        }

        val indexedGridItems =
            updatedFolderEblanApplicationInfoGridItems.mapIndexed { index, eblanApplicationInfoGridItem ->
                when (val data = eblanApplicationInfoGridItem.data) {
                    is FolderEblanApplicationInfoGridItemData.ApplicationInfo -> {
                        eblanApplicationInfoGridItem.copy(data = data.copy(folderIndex = index))
                    }

                    is FolderEblanApplicationInfoGridItemData.Folder -> {
                        eblanApplicationInfoGridItem.copy(data = data.copy(folderIndex = index))
                    }
                }
            }

        folderGridItemTransaction.upsertFolderGridItemsTransaction(folderEblanApplicationInfos = indexedGridItems)

        MoveFolderEblanApplicationInfoGridItemResult(
            isSuccess = true,
            folderEblanApplicationInfoGridItem = folderEblanApplicationInfoGridItem,
        )
    }
}

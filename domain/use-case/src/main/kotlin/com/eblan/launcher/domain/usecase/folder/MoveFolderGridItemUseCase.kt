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
import com.eblan.launcher.domain.model.grid.MoveGridItemResult
import com.eblan.launcher.domain.repository.GridRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import javax.inject.Inject

/** Reorders grid-folder items and persists their updated child indices. */
class MoveFolderGridItemUseCase @Inject constructor(
    private val gridRepository: GridRepository,
    @param:Dispatcher(EblanDispatchers.Default) private val defaultDispatcher: CoroutineDispatcher,
) {
    /**
     * Moves a folder child to the index indicated by its drag position on the current page.
     *
     * @param columns The number of columns in the folder popup grid.
     * @param rows The number of rows in the folder popup grid.
     * @param folderGridItems The ordered child items in the folder popup.
     * @param movingGridItem The child item being moved.
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
        folderGridItems: List<GridItem>,
        movingGridItem: GridItem,
        dragX: Int,
        dragY: Int,
        gridWidth: Int,
        gridHeight: Int,
        currentPage: Int,
    ): MoveGridItemResult = withContext(defaultDispatcher) {
        val gridItemsPerPage = columns * rows

        val cellWidth = gridWidth / columns
        val cellHeight = gridHeight / rows

        val targetColumn = dragX / cellWidth
        val targetRow = dragY / cellHeight

        val targetIndex =
            currentPage * gridItemsPerPage + targetRow * columns + targetColumn

        val updatedFolderGridItems = folderGridItems.toMutableList()

        val movingIndex =
            updatedFolderGridItems.indexOfFirst { currentGridItem ->
                currentGridItem.id == movingGridItem.id
            }

        if (movingIndex != -1) {
            updatedFolderGridItems.add(
                targetIndex.coerceIn(
                    0,
                    updatedFolderGridItems.size - 1,
                ),
                updatedFolderGridItems.removeAt(movingIndex),
            )
        }

        val indexedGridItems = updatedFolderGridItems.mapIndexed { index, gridItem ->
            when (val data = gridItem.data) {
                is GridItemData.ApplicationInfo -> gridItem.copy(data = data.copy(index = index))

                is GridItemData.ShortcutConfig -> gridItem.copy(data = data.copy(index = index))

                is GridItemData.ShortcutInfo ->
                    gridItem.copy(data = data.copy(index = index))

                is GridItemData.Folder ->
                    gridItem.copy(data = data.copy(index = index))

                else -> error("Unsupported move Folder GridItem ")
            }
        }

        gridRepository.upsertGridItems(indexedGridItems)

        MoveGridItemResult(
            isSuccess = true,
            movingGridItem = movingGridItem,
            conflictingGridItem = null,
        )
    }
}

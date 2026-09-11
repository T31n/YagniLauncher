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
import com.eblan.launcher.domain.model.FolderGridItemPopup
import com.eblan.launcher.domain.model.GridItem
import com.eblan.launcher.domain.model.GridItemData
import com.eblan.launcher.domain.model.MoveGridItemResult
import com.eblan.launcher.domain.repository.GridRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import javax.inject.Inject

class MoveFolderGridItemUseCase @Inject constructor(
    private val gridRepository: GridRepository,
    @param:Dispatcher(EblanDispatchers.Default) private val defaultDispatcher: CoroutineDispatcher,
) {
    suspend operator fun invoke(
        folderGridItemPopup: FolderGridItemPopup,
        movingGridItem: GridItem,
        dragX: Int,
        dragY: Int,
        gridWidth: Int,
        gridHeight: Int,
        currentPage: Int,
    ): MoveGridItemResult = withContext(defaultDispatcher) {
        val gridItemsPerPage = folderGridItemPopup.columns * folderGridItemPopup.rows

        val cellWidth = gridWidth / folderGridItemPopup.columns
        val cellHeight = gridHeight / folderGridItemPopup.rows

        val targetColumn = dragX / cellWidth
        val targetRow = dragY / cellHeight

        val targetIndex =
            currentPage * gridItemsPerPage + targetRow * folderGridItemPopup.columns + targetColumn

        val folderGridItems = folderGridItemPopup.gridItems.toMutableList()

        val movingIndex =
            folderGridItems.indexOfFirst {
                it.id == movingGridItem.id
            }

        if (movingIndex != -1) {
            folderGridItems.add(
                targetIndex.coerceIn(
                    0,
                    folderGridItems.size - 1,
                ),
                folderGridItems.removeAt(movingIndex),
            )
        }

        val indexedGridItems = folderGridItems.mapIndexed { index, gridItem ->
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

        gridRepository.upsertGridItems(gridItems = indexedGridItems)

        MoveGridItemResult(
            isSuccess = true,
            movingGridItem = movingGridItem,
            conflictingGridItem = null,
        )
    }
}

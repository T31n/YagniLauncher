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
import com.eblan.launcher.domain.model.application.EblanApplicationInfo
import com.eblan.launcher.domain.model.folder.FolderEblanApplicationInfo
import com.eblan.launcher.domain.model.folder.FolderEblanApplicationInfoGridItem
import com.eblan.launcher.domain.model.folder.FolderEblanApplicationInfoGridItemData
import com.eblan.launcher.domain.model.folder.FolderEblanApplicationInfoPopup
import com.eblan.launcher.domain.model.grid.MoveFolderEblanApplicationInfoGridItemResult
import com.eblan.launcher.domain.repository.FolderGridItemTransaction
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import javax.inject.Inject

class MoveFolderEblanApplicationInfoGridItemUseCase @Inject constructor(
    private val folderGridItemTransaction: FolderGridItemTransaction,
    @param:Dispatcher(EblanDispatchers.Default) private val defaultDispatcher: CoroutineDispatcher,
) {
    suspend operator fun invoke(
        folderGridItemPopup: FolderEblanApplicationInfoPopup,
        folderEblanApplicationInfoGridItem: FolderEblanApplicationInfoGridItem,
        dragX: Int,
        dragY: Int,
        gridWidth: Int,
        gridHeight: Int,
        currentPage: Int,
    ): MoveFolderEblanApplicationInfoGridItemResult = withContext(defaultDispatcher) {
        val gridItemsPerPage = folderGridItemPopup.columns * folderGridItemPopup.rows

        val cellWidth = gridWidth / folderGridItemPopup.columns
        val cellHeight = gridHeight / folderGridItemPopup.rows

        val targetColumn = dragX / cellWidth
        val targetRow = dragY / cellHeight

        val targetIndex =
            currentPage * gridItemsPerPage + targetRow * folderGridItemPopup.columns + targetColumn

        val folderEblanApplicationInfos =
            folderGridItemPopup.folderEblanApplicationInfos.toMutableList()

        val movingIndex =
            folderEblanApplicationInfos.indexOfFirst {
                it.id == folderEblanApplicationInfoGridItem.id
            }

        if (movingIndex != -1) {
            folderEblanApplicationInfos.add(
                targetIndex.coerceIn(
                    0,
                    folderEblanApplicationInfos.size - 1,
                ),
                folderEblanApplicationInfos.removeAt(movingIndex),
            )
        }

        val indexedGridItems =
            folderEblanApplicationInfos.mapIndexed { index, eblanApplicationInfoGridItem ->
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

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
package com.eblan.launcher.domain.usecase.grid

import com.eblan.launcher.domain.common.Dispatcher
import com.eblan.launcher.domain.common.EblanDispatchers
import com.eblan.launcher.domain.model.grid.GridItem
import com.eblan.launcher.domain.model.grid.GridItemData
import com.eblan.launcher.domain.model.grid.MoveGridItemResult
import com.eblan.launcher.domain.repository.FolderGridItemRepository
import com.eblan.launcher.domain.repository.GridRepository
import com.eblan.launcher.domain.usecase.util.getFolderGridItemsById
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import javax.inject.Inject
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

class UpdateGridItemsAfterMoveUseCase @Inject constructor(
    private val gridRepository: GridRepository,
    private val folderGridItemRepository: FolderGridItemRepository,
    @param:Dispatcher(EblanDispatchers.Default) private val defaultDispatcher: CoroutineDispatcher,
) {
    suspend operator fun invoke(moveGridItemResult: MoveGridItemResult) {
        withContext(defaultDispatcher) {
            val conflictingGridItem = moveGridItemResult.conflictingGridItem

            val movingGridItem = moveGridItemResult.movingGridItem

            if (conflictingGridItem != null) {
                when (conflictingGridItem.data) {
                    is GridItemData.Folder -> {
                        val folderGridItems = getFolderGridItemsById(
                            folderGridItemRepository = folderGridItemRepository,
                            folderId = conflictingGridItem.id,
                        )

                        addMovingGridItemIntoFolder(
                            conflictingGridItem = conflictingGridItem,
                            movingGridItem = movingGridItem,
                            folderGridItems = folderGridItems,
                        )
                    }

                    is GridItemData.ApplicationInfo,
                    is GridItemData.ShortcutConfig,
                    is GridItemData.ShortcutInfo,
                    is GridItemData.Widget,
                    -> {
                        createNewFolder(
                            conflictingGridItem = conflictingGridItem,
                            movingGridItem = movingGridItem,
                        )
                    }
                }
            } else {
                gridRepository.updateGridItem(gridItem = movingGridItem)
            }
        }
    }

    private suspend fun addMovingGridItemIntoFolder(
        conflictingGridItem: GridItem,
        movingGridItem: GridItem,
        folderGridItems: List<GridItem>,
    ) {
        val index = folderGridItems.maxOfOrNull {
            when (val folderData = it.data) {
                is GridItemData.ApplicationInfo -> folderData.index + 1
                is GridItemData.ShortcutConfig -> folderData.index + 1
                is GridItemData.ShortcutInfo -> folderData.index + 1
                is GridItemData.Folder -> folderData.index + 1
                else -> error("Unsupported addMovingGridItemIntoFolder")
            }
        } ?: 0

        val newData = when (val folderData = movingGridItem.data) {
            is GridItemData.ApplicationInfo -> folderData.copy(
                index = index,
                folderId = conflictingGridItem.id,
            )

            is GridItemData.ShortcutInfo -> folderData.copy(
                index = index,
                folderId = conflictingGridItem.id,
            )

            is GridItemData.ShortcutConfig -> folderData.copy(
                index = index,
                folderId = conflictingGridItem.id,
            )

            is GridItemData.Folder -> folderData.copy(
                index = index,
                folderId = conflictingGridItem.id,
            )

            else -> error("Unsupported addMovingGridItemIntoFolder")
        }

        gridRepository.updateGridItem(
            gridItem = movingGridItem.copy(
                associate = conflictingGridItem.associate,
                data = newData,
            ),
        )
    }

    @OptIn(ExperimentalUuidApi::class)
    private suspend fun createNewFolder(
        conflictingGridItem: GridItem,
        movingGridItem: GridItem,
    ) {
        val id = Uuid.random().toHexString()

        val conflictingData = when (val data = conflictingGridItem.data) {
            is GridItemData.ApplicationInfo -> {
                data.copy(
                    folderId = id,
                    index = 0,
                )
            }

            is GridItemData.ShortcutInfo -> {
                data.copy(
                    folderId = id,
                    index = 0,
                )
            }

            is GridItemData.ShortcutConfig -> {
                data.copy(
                    folderId = id,
                    index = 0,
                )
            }

            is GridItemData.Folder -> data.copy(
                folderId = id,
                index = 0,
            )

            else -> error("Unsupported createNewFolder")
        }

        val movingData = when (val data = movingGridItem.data) {
            is GridItemData.ApplicationInfo -> data.copy(
                folderId = id,
                index = 1,
            )

            is GridItemData.ShortcutInfo -> data.copy(
                folderId = id,
                index = 1,
            )

            is GridItemData.ShortcutConfig -> data.copy(
                folderId = id,
                index = 1,
            )

            is GridItemData.Folder -> data.copy(
                folderId = id,
                index = 1,
            )

            else -> error("Unsupported createNewFolder")
        }

        gridRepository.upsertGridItems(
            gridItems = listOf(
                conflictingGridItem.copy(
                    id = id,
                    data = GridItemData.Folder(
                        label = "New Folder",
                        icon = null,
                        index = -1,
                        folderId = null,
                    ),
                ),
                conflictingGridItem.copy(data = conflictingData),
                movingGridItem.copy(
                    associate = conflictingGridItem.associate,
                    data = movingData,
                ),
            ),
        )
    }
}

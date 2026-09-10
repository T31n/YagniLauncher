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
import com.eblan.launcher.domain.grid.getGridItemByCoordinates
import com.eblan.launcher.domain.grid.getRelativeResolveDirection
import com.eblan.launcher.domain.grid.getResolveDirectionByX
import com.eblan.launcher.domain.grid.rectanglesOverlap
import com.eblan.launcher.domain.grid.resolveConflicts
import com.eblan.launcher.domain.model.GridItem
import com.eblan.launcher.domain.model.GridItemData
import com.eblan.launcher.domain.model.MoveGridItemResult
import com.eblan.launcher.domain.model.ResolveDirection
import com.eblan.launcher.domain.repository.GridRepository
import com.eblan.launcher.domain.usecase.util.isTopLevel
import com.eblan.launcher.domain.usecase.util.toGridItems
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import javax.inject.Inject

class MoveGridItemUseCase @Inject constructor(
    private val gridRepository: GridRepository,
    @param:Dispatcher(EblanDispatchers.Default) private val defaultDispatcher: CoroutineDispatcher,
) {
    suspend operator fun invoke(
        movingGridItem: GridItem,
        x: Int,
        y: Int,
        columns: Int,
        rows: Int,
        gridWidth: Int,
        gridHeight: Int,
    ): MoveGridItemResult {
        return withContext(defaultDispatcher) {
            val gridItemsByPage = gridRepository.getGridItems().toGridItems().filter {
                it.isTopLevel() && it.page == movingGridItem.page &&
                    it.associate == movingGridItem.associate
            }.toMutableList()

            val index =
                gridItemsByPage.indexOfFirst {
                    it.id == movingGridItem.id
                }

            if (index != -1) {
                gridItemsByPage[index] = movingGridItem
            } else {
                gridItemsByPage.add(movingGridItem)
            }

            val conflictingGridItemByCoordinates = getGridItemByCoordinates(
                id = movingGridItem.id,
                gridItems = gridItemsByPage,
                columns = columns,
                rows = rows,
                x = x,
                y = y,
                gridWidth = gridWidth,
                gridHeight = gridHeight,
            )

            if (conflictingGridItemByCoordinates != null) {
                return@withContext handleConflictsOfGridItemCoordinates(
                    gridItems = gridItemsByPage,
                    movingGridItem = movingGridItem,
                    conflictingGridItem = conflictingGridItemByCoordinates,
                    x = x,
                    columns = columns,
                    rows = rows,
                    gridWidth = gridWidth,
                )
            }

            val conflictingGridItemBySpan = gridItemsByPage.find {
                it.id != movingGridItem.id && rectanglesOverlap(
                    moving = movingGridItem,
                    other = it,
                )
            }

            if (conflictingGridItemBySpan != null) {
                return@withContext handleConflictsOfGridItemSpan(
                    movingGridItem = movingGridItem,
                    conflictingGridItem = conflictingGridItemBySpan,
                    gridItems = gridItemsByPage,
                    columns = columns,
                    rows = rows,
                )
            }

            gridRepository.upsertGridItems(gridItems = gridItemsByPage)

            return@withContext MoveGridItemResult(
                isSuccess = true,
                movingGridItem = movingGridItem,
                conflictingGridItem = null,
            )
        }
    }

    private suspend fun handleConflictsOfGridItemCoordinates(
        gridItems: MutableList<GridItem>,
        movingGridItem: GridItem,
        conflictingGridItem: GridItem,
        x: Int,
        columns: Int,
        rows: Int,
        gridWidth: Int,
    ): MoveGridItemResult {
        val resolveDirection = getResolveDirectionByX(
            gridItem = conflictingGridItem,
            x = x,
            columns = columns,
            gridWidth = gridWidth,
        )

        return when (resolveDirection) {
            ResolveDirection.Left, ResolveDirection.Right -> {
                val resolvedConflicts = resolveConflicts(
                    gridItems = gridItems,
                    resolveDirection = resolveDirection,
                    movingGridItem = movingGridItem,
                    columns = columns,
                    rows = rows,
                )

                if (resolvedConflicts) {
                    gridRepository.upsertGridItems(gridItems = gridItems)
                }

                MoveGridItemResult(
                    isSuccess = resolvedConflicts,
                    movingGridItem = movingGridItem,
                    conflictingGridItem = null,
                )
            }

            ResolveDirection.Center -> {
                if (movingGridItem.data is GridItemData.Widget ||
                    conflictingGridItem.data is GridItemData.Widget
                ) {
                    return MoveGridItemResult(
                        isSuccess = false,
                        movingGridItem = movingGridItem,
                        conflictingGridItem = null,
                    )
                }

                gridRepository.upsertGridItems(gridItems = gridItems)

                MoveGridItemResult(
                    isSuccess = true,
                    movingGridItem = movingGridItem,
                    conflictingGridItem = conflictingGridItem,
                )
            }
        }
    }

    private suspend fun handleConflictsOfGridItemSpan(
        movingGridItem: GridItem,
        conflictingGridItem: GridItem,
        gridItems: MutableList<GridItem>,
        columns: Int,
        rows: Int,
    ): MoveGridItemResult {
        val resolveDirection = getRelativeResolveDirection(
            moving = movingGridItem,
            other = conflictingGridItem,
        ) ?: return MoveGridItemResult(
            isSuccess = false,
            movingGridItem = movingGridItem,
            conflictingGridItem = null,
        )

        val resolvedConflicts = resolveConflicts(
            gridItems = gridItems,
            resolveDirection = resolveDirection,
            movingGridItem = movingGridItem,
            columns = columns,
            rows = rows,
        )

        if (resolvedConflicts) {
            gridRepository.upsertGridItems(gridItems = gridItems)
        }

        return MoveGridItemResult(
            isSuccess = resolvedConflicts,
            movingGridItem = movingGridItem,
            conflictingGridItem = null,
        )
    }
}

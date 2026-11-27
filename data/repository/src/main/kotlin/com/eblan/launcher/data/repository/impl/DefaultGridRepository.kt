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
package com.eblan.launcher.data.repository.impl

import com.eblan.launcher.data.repository.mapper.asApplicationInfoGridItem
import com.eblan.launcher.data.repository.mapper.asFolderGridItem
import com.eblan.launcher.data.repository.mapper.asModel
import com.eblan.launcher.data.repository.mapper.asShortcutConfigGridItem
import com.eblan.launcher.data.repository.mapper.asShortcutInfoGridItem
import com.eblan.launcher.data.repository.mapper.asWidgetGridItem
import com.eblan.launcher.data.room.model.GridItemEntities
import com.eblan.launcher.data.room.transaction.GridTransaction
import com.eblan.launcher.domain.model.grid.ApplicationInfoGridItem
import com.eblan.launcher.domain.model.grid.FolderGridItem
import com.eblan.launcher.domain.model.grid.GridItem
import com.eblan.launcher.domain.model.grid.GridItemData
import com.eblan.launcher.domain.model.grid.GridItems
import com.eblan.launcher.domain.model.grid.ShortcutConfigGridItem
import com.eblan.launcher.domain.model.grid.ShortcutInfoGridItem
import com.eblan.launcher.domain.model.grid.WidgetGridItem
import com.eblan.launcher.domain.repository.ApplicationInfoGridItemRepository
import com.eblan.launcher.domain.repository.FolderGridItemRepository
import com.eblan.launcher.domain.repository.GridRepository
import com.eblan.launcher.domain.repository.ShortcutConfigGridItemRepository
import com.eblan.launcher.domain.repository.ShortcutInfoGridItemRepository
import com.eblan.launcher.domain.repository.WidgetGridItemRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

internal class DefaultGridRepository @Inject constructor(
    private val applicationInfoGridItemRepository: ApplicationInfoGridItemRepository,
    private val widgetGridItemRepository: WidgetGridItemRepository,
    private val shortcutInfoGridItemRepository: ShortcutInfoGridItemRepository,
    private val folderGridItemRepository: FolderGridItemRepository,
    private val shortcutConfigGridItemRepository: ShortcutConfigGridItemRepository,
    private val gridTransaction: GridTransaction,
) : GridRepository {
    override val gridItemsFlow: Flow<GridItems> =
        gridTransaction.gridItemEntitiesFlow.map { gridItemEntities ->
            gridItemEntities.asGridItems()
        }

    override suspend fun getGridItems(): GridItems = gridTransaction.withTransaction {
        GridItems(
            applicationInfoGridItems = applicationInfoGridItemRepository.getApplicationInfoGridItems(),
            widgetGridItems = widgetGridItemRepository.getWidgetGridItems(),
            shortcutInfoGridItems = shortcutInfoGridItemRepository.getShortcutInfoGridItems(),
            shortcutConfigGridItems = shortcutConfigGridItemRepository.getShortcutConfigGridItems(),
            folderGridItems = folderGridItemRepository.getFolderGridItems(),
        )
    }

    override suspend fun insertGridItem(gridItem: GridItem) {
        when (val data = gridItem.data) {
            is GridItemData.ApplicationInfo -> {
                applicationInfoGridItemRepository.insertApplicationInfoGridItem(
                    gridItem.asApplicationInfoGridItem(data),
                )
            }

            is GridItemData.Folder -> {
                folderGridItemRepository.insertFolderGridItem(
                    gridItem.asFolderGridItem(data),
                )
            }

            is GridItemData.ShortcutInfo -> {
                shortcutInfoGridItemRepository.insertShortcutInfoGridItem(
                    gridItem.asShortcutInfoGridItem(data),
                )
            }

            is GridItemData.Widget -> {
                widgetGridItemRepository.insertWidgetGridItem(
                    gridItem.asWidgetGridItem(data),
                )
            }

            is GridItemData.ShortcutConfig -> {
                shortcutConfigGridItemRepository.insertShortcutConfigGridItem(
                    gridItem.asShortcutConfigGridItem(data),
                )
            }
        }
    }

    override suspend fun updateGridItem(gridItem: GridItem) {
        when (val data = gridItem.data) {
            is GridItemData.ApplicationInfo -> {
                applicationInfoGridItemRepository.updateApplicationInfoGridItem(
                    gridItem.asApplicationInfoGridItem(data),
                )
            }

            is GridItemData.Folder -> {
                folderGridItemRepository.updateFolderGridItem(
                    gridItem.asFolderGridItem(data),
                )
            }

            is GridItemData.ShortcutInfo -> {
                shortcutInfoGridItemRepository.updateShortcutInfoGridItem(
                    gridItem.asShortcutInfoGridItem(data),
                )
            }

            is GridItemData.Widget -> {
                widgetGridItemRepository.updateWidgetGridItem(
                    gridItem.asWidgetGridItem(data),
                )
            }

            is GridItemData.ShortcutConfig -> {
                shortcutConfigGridItemRepository.updateShortcutConfigGridItem(
                    gridItem.asShortcutConfigGridItem(data),
                )
            }
        }
    }

    override suspend fun upsertGridItems(gridItems: List<GridItem>) {
        val applicationInfoGridItems = mutableListOf<ApplicationInfoGridItem>()

        val widgetGridItems = mutableListOf<WidgetGridItem>()

        val shortcutInfoGridItems = mutableListOf<ShortcutInfoGridItem>()

        val shortcutConfigGridItems = mutableListOf<ShortcutConfigGridItem>()

        val folderGridItems = mutableListOf<FolderGridItem>()

        gridItems.forEach { gridItem ->
            when (val data = gridItem.data) {
                is GridItemData.ApplicationInfo -> {
                    applicationInfoGridItems.add(
                        gridItem.asApplicationInfoGridItem(data),
                    )
                }

                is GridItemData.Folder -> {
                    folderGridItems.add(
                        gridItem.asFolderGridItem(data),
                    )
                }

                is GridItemData.Widget -> {
                    widgetGridItems.add(
                        gridItem.asWidgetGridItem(data),
                    )
                }

                is GridItemData.ShortcutInfo -> {
                    shortcutInfoGridItems.add(
                        gridItem.asShortcutInfoGridItem(data),
                    )
                }

                is GridItemData.ShortcutConfig -> {
                    shortcutConfigGridItems.add(
                        gridItem.asShortcutConfigGridItem(data),
                    )
                }
            }
        }

        gridTransaction.withTransaction {
            folderGridItemRepository.upsertFolderGridItems(
                folderGridItems,
            )

            applicationInfoGridItemRepository.upsertApplicationInfoGridItems(
                applicationInfoGridItems,
            )

            widgetGridItemRepository.upsertWidgetGridItems(
                widgetGridItems,
            )

            shortcutInfoGridItemRepository.upsertShortcutInfoGridItems(
                shortcutInfoGridItems,
            )

            shortcutConfigGridItemRepository.upsertShortcutConfigGridItems(
                shortcutConfigGridItems,
            )
        }
    }

    override suspend fun deleteGridItems(gridItems: List<GridItem>) {
        val applicationInfoGridItems = mutableListOf<ApplicationInfoGridItem>()

        val widgetGridItems = mutableListOf<WidgetGridItem>()

        val shortcutInfoGridItems = mutableListOf<ShortcutInfoGridItem>()

        val shortcutConfigGridItems = mutableListOf<ShortcutConfigGridItem>()

        val folderGridItems = mutableListOf<FolderGridItem>()

        gridItems.forEach { gridItem ->
            when (val data = gridItem.data) {
                is GridItemData.ApplicationInfo -> {
                    applicationInfoGridItems.add(
                        gridItem.asApplicationInfoGridItem(data),
                    )
                }

                is GridItemData.Folder -> {
                    folderGridItems.add(
                        gridItem.asFolderGridItem(data),
                    )
                }

                is GridItemData.Widget -> {
                    widgetGridItems.add(
                        gridItem.asWidgetGridItem(data),
                    )
                }

                is GridItemData.ShortcutInfo -> {
                    shortcutInfoGridItems.add(
                        gridItem.asShortcutInfoGridItem(data),
                    )
                }

                is GridItemData.ShortcutConfig -> {
                    shortcutConfigGridItems.add(
                        gridItem.asShortcutConfigGridItem(data),
                    )
                }
            }
        }

        gridTransaction.withTransaction {
            applicationInfoGridItemRepository.deleteApplicationInfoGridItems(
                applicationInfoGridItems,
            )

            widgetGridItemRepository.deleteWidgetGridItems(
                widgetGridItems,
            )

            shortcutInfoGridItemRepository.deleteShortcutInfoGridItems(
                shortcutInfoGridItems,
            )

            shortcutConfigGridItemRepository.deleteShortcutConfigGridItems(
                shortcutConfigGridItems,
            )

            folderGridItemRepository.deleteFolderGridItems(
                folderGridItems,
            )
        }
    }

    override suspend fun deleteGridItem(gridItem: GridItem) {
        when (val data = gridItem.data) {
            is GridItemData.ApplicationInfo -> {
                applicationInfoGridItemRepository.deleteApplicationInfoGridItem(
                    gridItem.asApplicationInfoGridItem(data),
                )
            }

            is GridItemData.Folder -> {
                folderGridItemRepository.deleteFolderGridItem(
                    gridItem.asFolderGridItem(data),
                )
            }

            is GridItemData.ShortcutInfo -> {
                shortcutInfoGridItemRepository.deleteShortcutInfoGridItem(
                    gridItem.asShortcutInfoGridItem(data),
                )
            }

            is GridItemData.Widget -> {
                widgetGridItemRepository.deleteWidgetGridItem(
                    gridItem.asWidgetGridItem(data),
                )
            }

            is GridItemData.ShortcutConfig -> {
                shortcutConfigGridItemRepository.deleteShortcutConfigGridItem(
                    gridItem.asShortcutConfigGridItem(data),
                )
            }
        }
    }

    override suspend fun upsertGridItem(gridItem: GridItem) {
        when (val data = gridItem.data) {
            is GridItemData.ApplicationInfo -> {
                applicationInfoGridItemRepository.upsertApplicationInfoGridItem(
                    gridItem.asApplicationInfoGridItem(data),
                )
            }

            is GridItemData.Folder -> {
                folderGridItemRepository.upsertFolderGridItem(
                    gridItem.asFolderGridItem(data),
                )
            }

            is GridItemData.ShortcutInfo -> {
                shortcutInfoGridItemRepository.upsertShortcutInfoGridItem(
                    gridItem.asShortcutInfoGridItem(data),
                )
            }

            is GridItemData.Widget -> {
                widgetGridItemRepository.upsertWidgetGridItem(
                    gridItem.asWidgetGridItem(data),
                )
            }

            is GridItemData.ShortcutConfig -> {
                shortcutConfigGridItemRepository.upsertShortcutConfigGridItem(
                    gridItem.asShortcutConfigGridItem(data),
                )
            }
        }
    }

    override suspend fun deleteGridItemById(gridItem: GridItem) {
        when (gridItem.data) {
            is GridItemData.ApplicationInfo -> applicationInfoGridItemRepository.deleteApplicationInfoGridItemById(
                gridItem.id,
            )

            is GridItemData.Folder ->
                folderGridItemRepository.deleteFolderGridItemById(gridItem.id)

            is GridItemData.ShortcutInfo ->
                shortcutInfoGridItemRepository.deleteShortcutInfoGridItemById(gridItem.id)

            is GridItemData.Widget ->
                widgetGridItemRepository.deleteWidgetGridItemById(gridItem.id)

            is GridItemData.ShortcutConfig ->
                shortcutConfigGridItemRepository.deleteShortcutConfigGridItemById(gridItem.id)
        }
    }

    override suspend fun <T> withTransaction(block: suspend () -> T): T = gridTransaction.withTransaction {
        block()
    }

    private fun GridItemEntities.asGridItems(): GridItems {
        val applicationInfoGridItems = applicationInfoGridItemEntities.map { currentApplicationInfoGridItemEntity ->
            currentApplicationInfoGridItemEntity.asModel()
        }

        val widgetGridItems = widgetGridItemEntities.map { currentWidgetGridItemEntity ->
            currentWidgetGridItemEntity.asModel()
        }

        val shortcutInfoGridItems = shortcutInfoGridItemEntities.map { currentShortcutInfoGridItemEntity ->
            currentShortcutInfoGridItemEntity.asModel()
        }

        val shortcutConfigGridItems = shortcutConfigGridItemEntities.map { currentShortcutConfigGridItemEntity ->
            currentShortcutConfigGridItemEntity.asModel()
        }

        val folderGridItems = folderGridItemEntities.map { currentFolderGridItemEntity ->
            currentFolderGridItemEntity.asModel()
        }

        return GridItems(
            applicationInfoGridItems = applicationInfoGridItems,
            widgetGridItems = widgetGridItems,
            shortcutInfoGridItems = shortcutInfoGridItems,
            shortcutConfigGridItems = shortcutConfigGridItems,
            folderGridItems = folderGridItems,
        )
    }
}

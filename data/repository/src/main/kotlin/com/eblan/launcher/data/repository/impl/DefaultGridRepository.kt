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
import com.eblan.launcher.data.repository.mapper.asEntity
import com.eblan.launcher.data.repository.mapper.asFolderGridItem
import com.eblan.launcher.data.repository.mapper.asModel
import com.eblan.launcher.data.repository.mapper.asShortcutConfigGridItem
import com.eblan.launcher.data.repository.mapper.asShortcutInfoGridItem
import com.eblan.launcher.data.repository.mapper.asWidgetGridItem
import com.eblan.launcher.data.room.GridItemTransaction
import com.eblan.launcher.data.room.entity.ApplicationInfoGridItemEntity
import com.eblan.launcher.data.room.entity.FolderGridItemEntity
import com.eblan.launcher.data.room.entity.ShortcutConfigGridItemEntity
import com.eblan.launcher.data.room.entity.ShortcutInfoGridItemEntity
import com.eblan.launcher.data.room.entity.WidgetGridItemEntity
import com.eblan.launcher.data.room.model.GridItemEntities
import com.eblan.launcher.domain.model.GridItem
import com.eblan.launcher.domain.model.GridItemData
import com.eblan.launcher.domain.model.GridItems
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
    private val gridItemTransaction: GridItemTransaction,
) : GridRepository {
    override val gridItemsFlow: Flow<GridItems> =
        gridItemTransaction.gridItemEntitiesFlow.map { gridItemEntities ->
            gridItemEntities.asGridItems()
        }

    override suspend fun getGridItems(): GridItems = gridItemTransaction.getGridItemEntities().asGridItems()

    override suspend fun insertGridItem(gridItem: GridItem) {
        when (val data = gridItem.data) {
            is GridItemData.ApplicationInfo -> {
                applicationInfoGridItemRepository.insertApplicationInfoGridItem(
                    applicationInfoGridItem = gridItem.asApplicationInfoGridItem(data = data),
                )
            }

            is GridItemData.Folder -> {
                folderGridItemRepository.insertFolderGridItem(
                    folderGridItem = gridItem.asFolderGridItem(data = data),
                )
            }

            is GridItemData.ShortcutInfo -> {
                shortcutInfoGridItemRepository.insertShortcutInfoGridItem(
                    shortcutInfoGridItem = gridItem.asShortcutInfoGridItem(data = data),
                )
            }

            is GridItemData.Widget -> {
                widgetGridItemRepository.insertWidgetGridItem(
                    widgetGridItem = gridItem.asWidgetGridItem(data = data),
                )
            }

            is GridItemData.ShortcutConfig -> {
                shortcutConfigGridItemRepository.insertShortcutConfigGridItem(
                    shortcutConfigGridItem = gridItem.asShortcutConfigGridItem(data = data),
                )
            }
        }
    }

    override suspend fun updateGridItem(gridItem: GridItem) {
        when (val data = gridItem.data) {
            is GridItemData.ApplicationInfo -> {
                applicationInfoGridItemRepository.updateApplicationInfoGridItem(
                    applicationInfoGridItem = gridItem.asApplicationInfoGridItem(data = data),
                )
            }

            is GridItemData.Folder -> {
                folderGridItemRepository.updateFolderGridItem(
                    folderGridItem = gridItem.asFolderGridItem(data = data),
                )
            }

            is GridItemData.ShortcutInfo -> {
                shortcutInfoGridItemRepository.updateShortcutInfoGridItem(
                    shortcutInfoGridItem = gridItem.asShortcutInfoGridItem(data = data),
                )
            }

            is GridItemData.Widget -> {
                widgetGridItemRepository.updateWidgetGridItem(
                    widgetGridItem = gridItem.asWidgetGridItem(data = data),
                )
            }

            is GridItemData.ShortcutConfig -> {
                shortcutConfigGridItemRepository.updateShortcutConfigGridItem(
                    shortcutConfigGridItem = gridItem.asShortcutConfigGridItem(data = data),
                )
            }
        }
    }

    override suspend fun upsertGridItems(gridItems: List<GridItem>) {
        val applicationInfoGridItemEntities = mutableListOf<ApplicationInfoGridItemEntity>()

        val widgetGridItemEntities = mutableListOf<WidgetGridItemEntity>()

        val shortcutInfoGridItemEntities = mutableListOf<ShortcutInfoGridItemEntity>()

        val shortcutConfigGridItemEntities = mutableListOf<ShortcutConfigGridItemEntity>()

        val folderGridItemEntities = mutableListOf<FolderGridItemEntity>()

        gridItems.forEach { gridItem ->
            when (val data = gridItem.data) {
                is GridItemData.ApplicationInfo -> {
                    applicationInfoGridItemEntities.add(
                        gridItem.asApplicationInfoGridItem(data = data).asEntity(),
                    )
                }

                is GridItemData.Folder -> {
                    folderGridItemEntities.add(
                        gridItem.asFolderGridItem(data = data).asEntity(),
                    )
                }

                is GridItemData.Widget -> {
                    widgetGridItemEntities.add(
                        gridItem.asWidgetGridItem(data = data).asEntity(),
                    )
                }

                is GridItemData.ShortcutInfo -> {
                    shortcutInfoGridItemEntities.add(
                        gridItem.asShortcutInfoGridItem(data = data).asEntity(),
                    )
                }

                is GridItemData.ShortcutConfig -> {
                    shortcutConfigGridItemEntities.add(
                        gridItem.asShortcutConfigGridItem(data = data).asEntity(),
                    )
                }
            }
        }

        gridItemTransaction.upsertGridItemEntitiesTransaction(
            applicationInfoGridItemEntities = applicationInfoGridItemEntities,
            widgetGridItemEntities = widgetGridItemEntities,
            shortcutInfoGridItemEntities = shortcutInfoGridItemEntities,
            shortcutConfigGridItemEntities = shortcutConfigGridItemEntities,
            folderGridItemEntities = folderGridItemEntities,
        )
    }

    override suspend fun deleteGridItems(gridItems: List<GridItem>) {
        val applicationInfoGridItemEntities = mutableListOf<ApplicationInfoGridItemEntity>()

        val widgetGridItemEntities = mutableListOf<WidgetGridItemEntity>()

        val shortcutInfoGridItemEntities = mutableListOf<ShortcutInfoGridItemEntity>()

        val shortcutConfigGridItemEntities = mutableListOf<ShortcutConfigGridItemEntity>()

        val folderGridItemEntities = mutableListOf<FolderGridItemEntity>()

        gridItems.forEach { gridItem ->
            when (val data = gridItem.data) {
                is GridItemData.ApplicationInfo -> {
                    applicationInfoGridItemEntities.add(
                        gridItem.asApplicationInfoGridItem(data = data).asEntity(),
                    )
                }

                is GridItemData.Folder -> {
                    folderGridItemEntities.add(
                        gridItem.asFolderGridItem(data = data).asEntity(),
                    )
                }

                is GridItemData.Widget -> {
                    widgetGridItemEntities.add(
                        gridItem.asWidgetGridItem(data = data).asEntity(),
                    )
                }

                is GridItemData.ShortcutInfo -> {
                    shortcutInfoGridItemEntities.add(
                        gridItem.asShortcutInfoGridItem(data = data).asEntity(),
                    )
                }

                is GridItemData.ShortcutConfig -> {
                    shortcutConfigGridItemEntities.add(
                        gridItem.asShortcutConfigGridItem(data = data).asEntity(),
                    )
                }
            }
        }

        gridItemTransaction.deleteGridItemEntitiesTransaction(
            applicationInfoGridItemEntities = applicationInfoGridItemEntities,
            widgetGridItemEntities = widgetGridItemEntities,
            shortcutInfoGridItemEntities = shortcutInfoGridItemEntities,
            shortcutConfigGridItemEntities = shortcutConfigGridItemEntities,
            folderGridItemEntities = folderGridItemEntities,
        )
    }

    override suspend fun deleteGridItem(gridItem: GridItem) {
        when (val data = gridItem.data) {
            is GridItemData.ApplicationInfo -> {
                applicationInfoGridItemRepository.deleteApplicationInfoGridItem(
                    applicationInfoGridItem = gridItem.asApplicationInfoGridItem(data = data),
                )
            }

            is GridItemData.Folder -> {
                folderGridItemRepository.deleteFolderGridItem(
                    folderGridItem = gridItem.asFolderGridItem(data = data),
                )
            }

            is GridItemData.ShortcutInfo -> {
                shortcutInfoGridItemRepository.deleteShortcutInfoGridItem(
                    shortcutInfoGridItem = gridItem.asShortcutInfoGridItem(data = data),
                )
            }

            is GridItemData.Widget -> {
                widgetGridItemRepository.deleteWidgetGridItem(
                    widgetGridItem = gridItem.asWidgetGridItem(data = data),
                )
            }

            is GridItemData.ShortcutConfig -> {
                shortcutConfigGridItemRepository.deleteShortcutConfigGridItem(
                    shortcutConfigGridItem = gridItem.asShortcutConfigGridItem(data = data),
                )
            }
        }
    }

    override suspend fun upsertGridItem(gridItem: GridItem) {
        when (val data = gridItem.data) {
            is GridItemData.ApplicationInfo -> {
                applicationInfoGridItemRepository.upsertApplicationInfoGridItem(
                    applicationInfoGridItem = gridItem.asApplicationInfoGridItem(data = data),
                )
            }

            is GridItemData.Folder -> {
                folderGridItemRepository.upsertFolderGridItem(
                    folderGridItem = gridItem.asFolderGridItem(data = data),
                )
            }

            is GridItemData.ShortcutInfo -> {
                shortcutInfoGridItemRepository.upsertShortcutInfoGridItem(
                    shortcutInfoGridItem = gridItem.asShortcutInfoGridItem(data = data),
                )
            }

            is GridItemData.Widget -> {
                widgetGridItemRepository.upsertWidgetGridItem(
                    widgetGridItem = gridItem.asWidgetGridItem(data = data),
                )
            }

            is GridItemData.ShortcutConfig -> {
                shortcutConfigGridItemRepository.upsertShortcutConfigGridItem(
                    shortcutConfigGridItem = gridItem.asShortcutConfigGridItem(data = data),
                )
            }
        }
    }

    override suspend fun deleteGridItemById(gridItem: GridItem) {
        when (gridItem.data) {
            is GridItemData.ApplicationInfo -> applicationInfoGridItemRepository.deleteApplicationInfoGridItemById(
                id = gridItem.id,
            )

            is GridItemData.Folder ->
                folderGridItemRepository.deleteFolderGridItemById(id = gridItem.id)

            is GridItemData.ShortcutInfo ->
                shortcutInfoGridItemRepository.deleteShortcutInfoGridItemById(id = gridItem.id)

            is GridItemData.Widget ->
                widgetGridItemRepository.deleteWidgetGridItemById(id = gridItem.id)

            is GridItemData.ShortcutConfig ->
                shortcutConfigGridItemRepository.deleteShortcutConfigGridItemById(id = gridItem.id)
        }
    }

    private fun GridItemEntities.asGridItems(): GridItems {
        val applicationInfoGridItems = applicationInfoGridItemEntities.map {
            it.asModel()
        }

        val widgetGridItems = widgetGridItemEntities.map {
            it.asModel()
        }

        val shortcutInfoGridItems = shortcutInfoGridItemEntities.map {
            it.asModel()
        }

        val shortcutConfigGridItems = shortcutConfigGridItemEntities.map {
            it.asModel()
        }

        val folderGridItems = folderGridItemEntities.map {
            it.asModel()
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

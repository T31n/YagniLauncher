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

import com.eblan.launcher.data.repository.mapper.asEntity
import com.eblan.launcher.data.repository.mapper.asModel
import com.eblan.launcher.data.room.dao.ShortcutInfoGridItemDao
import com.eblan.launcher.domain.common.GridLock
import com.eblan.launcher.domain.model.grid.PartialShortcutInfoGridItem
import com.eblan.launcher.domain.model.grid.ShortcutInfoGridItem
import com.eblan.launcher.domain.repository.ShortcutInfoGridItemRepository
import javax.inject.Inject

internal class DefaultShortcutInfoGridItemRepository @Inject constructor(
    private val shortcutInfoGridItemDao: ShortcutInfoGridItemDao,
    private val gridLock: GridLock,
) : ShortcutInfoGridItemRepository {
    override suspend fun getShortcutInfoGridItems(): List<ShortcutInfoGridItem> = shortcutInfoGridItemDao.getShortcutInfoGridItemEntities().map {
        it.asModel()
    }

    override suspend fun updateShortcutInfoGridItem(shortcutInfoGridItem: ShortcutInfoGridItem) = gridLock.withLock {
        shortcutInfoGridItemDao.updateShortcutInfoGridItemEntity(
            shortcutInfoGridItem.asEntity(),
        )
    }

    override suspend fun deleteShortcutInfoGridItems(shortcutInfoGridItems: List<ShortcutInfoGridItem>) = gridLock.withLock {
        val entities = shortcutInfoGridItems.map {
            it.asEntity()
        }

        shortcutInfoGridItemDao.deleteShortcutInfoGridItemEntities(entities = entities)
    }

    override suspend fun deleteShortcutInfoGridItem(shortcutInfoGridItem: ShortcutInfoGridItem) = gridLock.withLock {
        shortcutInfoGridItemDao.deleteShortcutInfoGridItemEntity(entity = shortcutInfoGridItem.asEntity())
    }

    override suspend fun getShortcutInfoGridItemsByPackageName(
        serialNumber: Long,
        packageName: String,
    ): List<ShortcutInfoGridItem> = shortcutInfoGridItemDao.getShortcutInfoGridItemEntitiesByPackageName(
        serialNumber = serialNumber,
        packageName = packageName,
    ).map {
        it.asModel()
    }

    override suspend fun deleteShortcutInfoGridItemByPackageName(
        serialNumber: Long,
        packageName: String,
    ) = gridLock.withLock {
        shortcutInfoGridItemDao.deleteShortcutInfoGridItemEntityByPackageName(
            serialNumber = serialNumber,
            packageName = packageName,
        )
    }

    override suspend fun updatePartialShortcutInfoGridItems(partialShortcutInfoGridItems: List<PartialShortcutInfoGridItem>) = gridLock.withLock {
        shortcutInfoGridItemDao.updatePartialShortcutInfoGridItems(partialShortcutInfoGridItems = partialShortcutInfoGridItems)
    }

    override suspend fun insertShortcutInfoGridItem(shortcutInfoGridItem: ShortcutInfoGridItem) = gridLock.withLock {
        shortcutInfoGridItemDao.insertShortcutInfoGridItemEntity(entity = shortcutInfoGridItem.asEntity())
    }

    override suspend fun upsertShortcutInfoGridItem(shortcutInfoGridItem: ShortcutInfoGridItem) = gridLock.withLock {
        shortcutInfoGridItemDao.upsertShortcutInfoGridItemEntity(entity = shortcutInfoGridItem.asEntity())
    }

    override suspend fun deleteShortcutInfoGridItemById(id: String) = gridLock.withLock {
        shortcutInfoGridItemDao.deleteShortcutInfoGridItemEntityById(id = id)
    }
}

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
import com.eblan.launcher.data.room.dao.ShortcutConfigGridItemDao
import com.eblan.launcher.domain.common.GridLock
import com.eblan.launcher.domain.model.grid.PartialShortcutConfigGridItem
import com.eblan.launcher.domain.model.grid.ShortcutConfigGridItem
import com.eblan.launcher.domain.repository.ShortcutConfigGridItemRepository
import javax.inject.Inject

internal class DefaultShortcutConfigGridItemRepository @Inject constructor(
    private val shortcutConfigGridItemDao: ShortcutConfigGridItemDao,
    private val gridLock: GridLock,
) : ShortcutConfigGridItemRepository {
    override suspend fun getShortcutConfigGridItems(): List<ShortcutConfigGridItem> = shortcutConfigGridItemDao.getShortcutConfigGridItemEntities().map {
        it.asModel()
    }

    override suspend fun updateShortcutConfigGridItem(shortcutConfigGridItem: ShortcutConfigGridItem) = gridLock.withLock {
        shortcutConfigGridItemDao.updateShortcutConfigGridItemEntity(
            shortcutConfigGridItem.asEntity(),
        )
    }

    override suspend fun deleteShortcutConfigGridItems(shortcutConfigGridItems: List<ShortcutConfigGridItem>) = gridLock.withLock {
        val entities = shortcutConfigGridItems.map {
            it.asEntity()
        }

        shortcutConfigGridItemDao.deleteShortcutConfigGridItemEntities(entities = entities)
    }

    override suspend fun deleteShortcutConfigGridItem(shortcutConfigGridItem: ShortcutConfigGridItem) = gridLock.withLock {
        shortcutConfigGridItemDao.deleteShortcutConfigGridItemEntity(entity = shortcutConfigGridItem.asEntity())
    }

    override suspend fun getShortcutConfigGridItemsByPackageName(
        serialNumber: Long,
        packageName: String,
    ): List<ShortcutConfigGridItem> = shortcutConfigGridItemDao.getShortcutConfigGridItemEntitiesByPackageName(
        serialNumber = serialNumber,
        packageName = packageName,
    ).map {
        it.asModel()
    }

    override suspend fun deleteShortcutConfigGridItemByPackageName(
        serialNumber: Long,
        packageName: String,
    ) = gridLock.withLock {
        shortcutConfigGridItemDao.deleteShortcutConfigGridItemEntityPackageName(
            serialNumber = serialNumber,
            packageName = packageName,
        )
    }

    override suspend fun updatePartialShortcutConfigGridItems(
        partialShortcutConfigGridItems: List<PartialShortcutConfigGridItem>,
    ) = gridLock.withLock {
        shortcutConfigGridItemDao.updatePartialShortcutConfigGridItems(
            partialShortcutConfigGridItems = partialShortcutConfigGridItems,
        )
    }

    override suspend fun insertShortcutConfigGridItem(shortcutConfigGridItem: ShortcutConfigGridItem) = gridLock.withLock {
        shortcutConfigGridItemDao.insertShortcutConfigGridItemEntity(entity = shortcutConfigGridItem.asEntity())
    }

    override suspend fun upsertShortcutConfigGridItem(shortcutConfigGridItem: ShortcutConfigGridItem) = gridLock.withLock {
        shortcutConfigGridItemDao.upsertShortcutConfigGridItemEntity(entity = shortcutConfigGridItem.asEntity())
    }

    override suspend fun deleteShortcutConfigGridItemById(id: String) = gridLock.withLock {
        shortcutConfigGridItemDao.deleteShortcutConfigGridItemEntityById(id = id)
    }
}

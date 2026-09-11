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
import com.eblan.launcher.domain.model.PartialShortcutConfigGridItem
import com.eblan.launcher.domain.model.ShortcutConfigGridItem
import com.eblan.launcher.domain.repository.ShortcutConfigGridItemRepository
import javax.inject.Inject

internal class DefaultShortcutConfigGridItemRepository @Inject constructor(private val shortcutConfigGridItemDao: ShortcutConfigGridItemDao) : ShortcutConfigGridItemRepository {
    override suspend fun getShortcutConfigGridItems(): List<ShortcutConfigGridItem> = shortcutConfigGridItemDao.getShortcutConfigGridItemEntities().map {
        it.asModel()
    }

    override suspend fun updateShortcutConfigGridItem(shortcutConfigGridItem: ShortcutConfigGridItem) {
        shortcutConfigGridItemDao.updateShortcutConfigGridItemEntity(
            shortcutConfigGridItem.asEntity(),
        )
    }

    override suspend fun deleteShortcutConfigGridItems(shortcutConfigGridItems: List<ShortcutConfigGridItem>) {
        val entities = shortcutConfigGridItems.map {
            it.asEntity()
        }

        shortcutConfigGridItemDao.deleteShortcutConfigGridItemEntities(entities = entities)
    }

    override suspend fun deleteShortcutConfigGridItem(shortcutConfigGridItem: ShortcutConfigGridItem) {
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

    override suspend fun deleteShortcutConfigGridItem(
        serialNumber: Long,
        packageName: String,
    ) {
        shortcutConfigGridItemDao.deleteShortcutConfigGridItemEntity(
            serialNumber = serialNumber,
            packageName = packageName,
        )
    }

    override suspend fun updatePartialShortcutConfigGridItems(
        partialShortcutConfigGridItems: List<PartialShortcutConfigGridItem>,
    ) {
        shortcutConfigGridItemDao.updatePartialShortcutConfigGridItems(
            partialShortcutConfigGridItems = partialShortcutConfigGridItems,
        )
    }

    override suspend fun insertShortcutConfigGridItem(shortcutConfigGridItem: ShortcutConfigGridItem) {
        shortcutConfigGridItemDao.insertShortcutConfigGridItemEntity(entity = shortcutConfigGridItem.asEntity())
    }

    override suspend fun upsertShortcutConfigGridItem(shortcutConfigGridItem: ShortcutConfigGridItem) {
        shortcutConfigGridItemDao.upsertShortcutConfigGridItemEntity(entity = shortcutConfigGridItem.asEntity())
    }

    override suspend fun deleteShortcutConfigGridItemById(id: String) {
        shortcutConfigGridItemDao.deleteShortcutConfigGridItemEntityById(id = id)
    }
}

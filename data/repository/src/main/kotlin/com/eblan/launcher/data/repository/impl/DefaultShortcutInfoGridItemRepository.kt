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
import com.eblan.launcher.domain.model.PartialShortcutInfoGridItem
import com.eblan.launcher.domain.model.ShortcutInfoGridItem
import com.eblan.launcher.domain.repository.ShortcutInfoGridItemRepository
import javax.inject.Inject

internal class DefaultShortcutInfoGridItemRepository @Inject constructor(private val shortcutInfoGridItemDao: ShortcutInfoGridItemDao) : ShortcutInfoGridItemRepository {
    override suspend fun getShortcutInfoGridItems(): List<ShortcutInfoGridItem> = shortcutInfoGridItemDao.getShortcutInfoGridItemEntities().map {
        it.asModel()
    }

    override suspend fun updateShortcutInfoGridItem(shortcutInfoGridItem: ShortcutInfoGridItem) {
        shortcutInfoGridItemDao.updateShortcutInfoGridItemEntity(
            shortcutInfoGridItem.asEntity(),
        )
    }

    override suspend fun deleteShortcutInfoGridItems(shortcutInfoGridItems: List<ShortcutInfoGridItem>) {
        val entities = shortcutInfoGridItems.map {
            it.asEntity()
        }

        shortcutInfoGridItemDao.deleteShortcutInfoGridItemEntities(entities = entities)
    }

    override suspend fun deleteShortcutInfoGridItem(shortcutInfoGridItem: ShortcutInfoGridItem) {
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

    override suspend fun deleteShortcutInfoGridItem(
        serialNumber: Long,
        packageName: String,
    ) {
        shortcutInfoGridItemDao.deleteShortcutInfoGridItemEntity(
            serialNumber = serialNumber,
            packageName = packageName,
        )
    }

    override suspend fun updatePartialShortcutInfoGridItems(partialShortcutInfoGridItems: List<PartialShortcutInfoGridItem>) {
        shortcutInfoGridItemDao.updatePartialShortcutInfoGridItems(partialShortcutInfoGridItems = partialShortcutInfoGridItems)
    }

    override suspend fun insertShortcutInfoGridItem(shortcutInfoGridItem: ShortcutInfoGridItem) {
        shortcutInfoGridItemDao.insertShortcutInfoGridItemEntity(entity = shortcutInfoGridItem.asEntity())
    }

    override suspend fun upsertShortcutInfoGridItem(shortcutInfoGridItem: ShortcutInfoGridItem) {
        shortcutInfoGridItemDao.upsertShortcutInfoGridItemEntity(entity = shortcutInfoGridItem.asEntity())
    }

    override suspend fun deleteShortcutInfoGridItemById(id: String) {
        shortcutInfoGridItemDao.deleteShortcutInfoGridItemEntityById(id = id)
    }
}

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
import com.eblan.launcher.data.room.dao.WidgetGridItemDao
import com.eblan.launcher.domain.model.grid.WidgetGridItem
import com.eblan.launcher.domain.model.widget.PartialUpdateWidgetGridItem
import com.eblan.launcher.domain.repository.WidgetGridItemRepository
import javax.inject.Inject

internal class DefaultWidgetGridItemRepository @Inject constructor(private val widgetGridItemDao: WidgetGridItemDao) : WidgetGridItemRepository {
    override suspend fun getWidgetGridItems(): List<WidgetGridItem> = widgetGridItemDao.getWidgetGridItemEntities().map {
        it.asModel()
    }

    override suspend fun updateWidgetGridItem(widgetGridItem: WidgetGridItem) {
        widgetGridItemDao.updateWidgetGridItemEntity(
            widgetGridItem.asEntity(),
        )
    }

    override suspend fun deleteWidgetGridItemsByPackageName(widgetGridItems: List<WidgetGridItem>) {
        val entities = widgetGridItems.map {
            it.asEntity()
        }

        widgetGridItemDao.deleteWidgetGridItemEntities(entities = entities)
    }

    override suspend fun deleteWidgetGridItem(widgetGridItem: WidgetGridItem) {
        widgetGridItemDao.deleteWidgetGridItemEntity(entity = widgetGridItem.asEntity())
    }

    override suspend fun deleteWidgetGridItem(
        serialNumber: Long,
        packageName: String,
    ) {
        widgetGridItemDao.deleteWidgetGridItemEntity(
            serialNumber = serialNumber,
            packageName = packageName,
        )
    }

    override suspend fun updatePartialWidgetGridItems(partialUpdateWidgetGridItems: List<PartialUpdateWidgetGridItem>) {
        widgetGridItemDao.updatePartialWidgetGridItems(partialUpdateWidgetGridItems = partialUpdateWidgetGridItems)
    }

    override suspend fun insertWidgetGridItem(widgetGridItem: WidgetGridItem) {
        widgetGridItemDao.insertWidgetGridItemEntity(entity = widgetGridItem.asEntity())
    }

    override suspend fun upsertWidgetGridItem(widgetGridItem: WidgetGridItem) {
        widgetGridItemDao.upsertWidgetGridItemEntity(entity = widgetGridItem.asEntity())
    }

    override suspend fun deleteWidgetGridItemById(id: String) {
        widgetGridItemDao.deleteWidgetGridItemEntityById(id = id)
    }
}

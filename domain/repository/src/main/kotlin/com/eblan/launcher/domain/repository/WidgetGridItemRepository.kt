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
package com.eblan.launcher.domain.repository

import com.eblan.launcher.domain.model.grid.WidgetGridItem
import com.eblan.launcher.domain.model.widget.PartialUpdateWidgetGridItem

interface WidgetGridItemRepository {
    suspend fun getWidgetGridItems(): List<WidgetGridItem>

    suspend fun updateWidgetGridItem(widgetGridItem: WidgetGridItem)

    suspend fun deleteWidgetGridItemsByPackageName(widgetGridItems: List<WidgetGridItem>)

    suspend fun deleteWidgetGridItem(widgetGridItem: WidgetGridItem)

    suspend fun deleteWidgetGridItem(
        serialNumber: Long,
        packageName: String,
    )

    suspend fun updatePartialWidgetGridItems(partialUpdateWidgetGridItems: List<PartialUpdateWidgetGridItem>)

    suspend fun insertWidgetGridItem(widgetGridItem: WidgetGridItem)

    suspend fun upsertWidgetGridItem(widgetGridItem: WidgetGridItem)

    suspend fun deleteWidgetGridItemById(id: String)
}

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

import com.eblan.launcher.domain.model.grid.PartialShortcutConfigGridItem
import com.eblan.launcher.domain.model.grid.ShortcutConfigGridItem

interface ShortcutConfigGridItemRepository {
    suspend fun getShortcutConfigGridItems(): List<ShortcutConfigGridItem>

    suspend fun updateShortcutConfigGridItem(shortcutConfigGridItem: ShortcutConfigGridItem)

    suspend fun deleteShortcutConfigGridItems(shortcutConfigGridItems: List<ShortcutConfigGridItem>)

    suspend fun deleteShortcutConfigGridItem(shortcutConfigGridItem: ShortcutConfigGridItem)

    suspend fun getShortcutConfigGridItemsByPackageName(
        serialNumber: Long,
        packageName: String,
    ): List<ShortcutConfigGridItem>

    suspend fun deleteShortcutConfigGridItemByPackageName(
        serialNumber: Long,
        packageName: String,
    )

    suspend fun updatePartialShortcutConfigGridItems(partialShortcutConfigGridItems: List<PartialShortcutConfigGridItem>)

    suspend fun insertShortcutConfigGridItem(shortcutConfigGridItem: ShortcutConfigGridItem)

    suspend fun upsertShortcutConfigGridItem(shortcutConfigGridItem: ShortcutConfigGridItem)

    suspend fun deleteShortcutConfigGridItemById(id: String)
}

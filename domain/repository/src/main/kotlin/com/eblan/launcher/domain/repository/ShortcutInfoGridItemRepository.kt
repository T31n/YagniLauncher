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

import com.eblan.launcher.domain.model.grid.PartialShortcutInfoGridItem
import com.eblan.launcher.domain.model.grid.ShortcutInfoGridItem

interface ShortcutInfoGridItemRepository {
    suspend fun getShortcutInfoGridItems(): List<ShortcutInfoGridItem>

    suspend fun updateShortcutInfoGridItem(shortcutInfoGridItem: ShortcutInfoGridItem)

    suspend fun deleteShortcutInfoGridItems(shortcutInfoGridItems: List<ShortcutInfoGridItem>)

    suspend fun deleteShortcutInfoGridItem(shortcutInfoGridItem: ShortcutInfoGridItem)

    suspend fun getShortcutInfoGridItemsByPackageName(
        serialNumber: Long,
        packageName: String,
    ): List<ShortcutInfoGridItem>

    suspend fun deleteShortcutInfoGridItemByPackageName(
        serialNumber: Long,
        packageName: String,
    )

    suspend fun updatePartialShortcutInfoGridItems(partialShortcutInfoGridItems: List<PartialShortcutInfoGridItem>)

    suspend fun insertShortcutInfoGridItem(shortcutInfoGridItem: ShortcutInfoGridItem)

    suspend fun upsertShortcutInfoGridItem(shortcutInfoGridItem: ShortcutInfoGridItem)

    suspend fun deleteShortcutInfoGridItemById(id: String)
}

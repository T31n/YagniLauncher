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
package com.eblan.launcher.data.room.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import androidx.room.Upsert
import com.eblan.launcher.data.room.entity.ShortcutInfoGridItemEntity
import com.eblan.launcher.domain.model.grid.PartialShortcutInfoGridItem

@Dao
interface ShortcutInfoGridItemDao {
    @Query("SELECT * FROM ShortcutInfoGridItemEntity")
    suspend fun getShortcutInfoGridItemEntities(): List<ShortcutInfoGridItemEntity>

    @Upsert
    suspend fun upsertShortcutInfoGridItemEntities(entities: List<ShortcutInfoGridItemEntity>)

    @Update
    suspend fun updateShortcutInfoGridItemEntity(entity: ShortcutInfoGridItemEntity)

    @Delete
    suspend fun deleteShortcutInfoGridItemEntities(entities: List<ShortcutInfoGridItemEntity>)

    @Delete
    suspend fun deleteShortcutInfoGridItemEntity(entity: ShortcutInfoGridItemEntity)

    @Query("SELECT * FROM ShortcutInfoGridItemEntity WHERE serialNumber = :serialNumber AND packageName = :packageName")
    suspend fun getShortcutInfoGridItemEntitiesByPackageName(
        serialNumber: Long,
        packageName: String,
    ): List<ShortcutInfoGridItemEntity>

    @Query("DELETE FROM ShortcutInfoGridItemEntity WHERE serialNumber = :serialNumber AND packageName = :packageName")
    suspend fun deleteShortcutInfoGridItemEntity(
        serialNumber: Long,
        packageName: String,
    )

    @Update(entity = ShortcutInfoGridItemEntity::class)
    suspend fun updatePartialShortcutInfoGridItems(partialShortcutInfoGridItems: List<PartialShortcutInfoGridItem>)

    @Insert
    suspend fun insertShortcutInfoGridItemEntity(entity: ShortcutInfoGridItemEntity)

    @Upsert
    suspend fun upsertShortcutInfoGridItemEntity(entity: ShortcutInfoGridItemEntity)

    @Query("DELETE FROM ShortcutInfoGridItemEntity WHERE id = :id")
    suspend fun deleteShortcutInfoGridItemEntityById(id: String)
}

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
import com.eblan.launcher.data.room.entity.ShortcutConfigGridItemEntity
import com.eblan.launcher.domain.model.grid.PartialShortcutConfigGridItem

@Dao
interface ShortcutConfigGridItemDao {
    @Query("SELECT * FROM ShortcutConfigGridItemEntity")
    suspend fun getShortcutConfigGridItemEntities(): List<ShortcutConfigGridItemEntity>

    @Upsert
    suspend fun upsertShortcutConfigGridItemEntities(entities: List<ShortcutConfigGridItemEntity>)

    @Update
    suspend fun updateShortcutConfigGridItemEntity(entity: ShortcutConfigGridItemEntity)

    @Delete
    suspend fun deleteShortcutConfigGridItemEntities(entities: List<ShortcutConfigGridItemEntity>)

    @Delete
    suspend fun deleteShortcutConfigGridItemEntity(entity: ShortcutConfigGridItemEntity)

    @Query("SELECT * FROM ShortcutConfigGridItemEntity WHERE serialNumber = :serialNumber AND packageName = :packageName")
    suspend fun getShortcutConfigGridItemEntitiesByPackageName(
        serialNumber: Long,
        packageName: String,
    ): List<ShortcutConfigGridItemEntity>

    @Query("DELETE FROM ShortcutConfigGridItemEntity WHERE serialNumber = :serialNumber AND packageName = :packageName")
    suspend fun deleteShortcutConfigGridItemEntityPackageName(
        serialNumber: Long,
        packageName: String,
    )

    @Update(entity = ShortcutConfigGridItemEntity::class)
    suspend fun updatePartialShortcutConfigGridItems(partialShortcutConfigGridItems: List<PartialShortcutConfigGridItem>)

    @Insert
    suspend fun insertShortcutConfigGridItemEntity(entity: ShortcutConfigGridItemEntity)

    @Upsert
    suspend fun upsertShortcutConfigGridItemEntity(entity: ShortcutConfigGridItemEntity)

    @Query("DELETE FROM ShortcutConfigGridItemEntity WHERE id = :id")
    suspend fun deleteShortcutConfigGridItemEntityById(id: String)
}

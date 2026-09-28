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
import com.eblan.launcher.data.room.entity.WidgetGridItemEntity
import com.eblan.launcher.domain.model.widget.PartialUpdateWidgetGridItem

@Dao
interface WidgetGridItemDao {
    @Query("SELECT * FROM WidgetGridItemEntity")
    suspend fun getWidgetGridItemEntities(): List<WidgetGridItemEntity>

    @Upsert
    suspend fun upsertWidgetGridItemEntities(entities: List<WidgetGridItemEntity>)

    @Update
    suspend fun updateWidgetGridItemEntity(entity: WidgetGridItemEntity)

    @Delete
    suspend fun deleteWidgetGridItemEntities(entities: List<WidgetGridItemEntity>)

    @Delete
    suspend fun deleteWidgetGridItemEntity(entity: WidgetGridItemEntity)

    @Query("DELETE FROM WidgetGridItemEntity WHERE serialNumber = :serialNumber AND packageName = :packageName")
    suspend fun deleteWidgetGridItemEntityByPackageName(
        serialNumber: Long,
        packageName: String,
    )

    @Update(entity = WidgetGridItemEntity::class)
    suspend fun updatePartialWidgetGridItems(partialUpdateWidgetGridItems: List<PartialUpdateWidgetGridItem>)

    @Insert
    suspend fun insertWidgetGridItemEntity(entity: WidgetGridItemEntity)

    @Upsert
    suspend fun upsertWidgetGridItemEntity(entity: WidgetGridItemEntity)

    @Query("DELETE FROM WidgetGridItemEntity WHERE id = :id")
    suspend fun deleteWidgetGridItemEntityById(id: String)
}

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
import androidx.room.Query
import androidx.room.Upsert
import com.eblan.launcher.data.room.entity.EblanShortcutConfigEntity
import com.eblan.launcher.domain.model.shortcutconfig.DeleteEblanShortcutConfig
import kotlinx.coroutines.flow.Flow

@Dao
interface EblanShortcutConfigDao {
    @Query("SELECT * FROM EblanShortcutConfigEntity")
    fun getEblanShortcutConfigEntitiesFlow(): Flow<List<EblanShortcutConfigEntity>>

    @Query("SELECT * FROM EblanShortcutConfigEntity")
    fun getEblanShortcutConfigEntities(): List<EblanShortcutConfigEntity>

    @Upsert
    suspend fun upsertEblanShortcutConfigEntities(entities: List<EblanShortcutConfigEntity>)

    @Query("DELETE FROM EblanShortcutConfigEntity WHERE serialNumber = :serialNumber AND packageName = :packageName")
    suspend fun deleteEblanShortcutConfigEntityByPackageName(
        serialNumber: Long,
        packageName: String,
    )

    @Delete(entity = EblanShortcutConfigEntity::class)
    suspend fun deleteEblanShortcutConfigEntities(deleteEblanShortcutConfigs: List<DeleteEblanShortcutConfig>)

    @Query("SELECT * FROM EblanShortcutConfigEntity WHERE serialNumber = :serialNumber AND packageName = :packageName")
    suspend fun getEblanShortcutConfigEntitiesByPackageName(
        serialNumber: Long,
        packageName: String,
    ): List<EblanShortcutConfigEntity>
}

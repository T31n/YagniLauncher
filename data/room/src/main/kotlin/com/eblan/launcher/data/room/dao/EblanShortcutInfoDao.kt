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
import com.eblan.launcher.data.room.entity.EblanShortcutInfoEntity
import com.eblan.launcher.domain.model.shortcutinfo.DeleteEblanShortcutInfo
import kotlinx.coroutines.flow.Flow

@Dao
interface EblanShortcutInfoDao {

    @Query("SELECT * FROM EblanShortcutInfoEntity")
    fun getEblanShortcutInfoEntitiesFlow(): Flow<List<EblanShortcutInfoEntity>>

    @Query("SELECT * FROM EblanShortcutInfoEntity")
    fun getEblanShortcutInfoEntities(): List<EblanShortcutInfoEntity>

    @Upsert
    suspend fun upsertEblanShortcutInfoEntities(entities: List<EblanShortcutInfoEntity>)

    @Delete(entity = EblanShortcutInfoEntity::class)
    suspend fun deleteEblanShortcutInfoEntities(deleteEblanShortcutInfos: List<DeleteEblanShortcutInfo>)

    @Query("SELECT * FROM EblanShortcutInfoEntity WHERE serialNumber = :serialNumber AND packageName = :packageName")
    suspend fun getEblanShortcutInfoEntities(
        serialNumber: Long,
        packageName: String,
    ): List<EblanShortcutInfoEntity>

    @Query("DELETE FROM EblanShortcutInfoEntity WHERE serialNumber = :serialNumber AND packageName = :packageName")
    suspend fun deleteEblanShortcutInfoEntities(
        serialNumber: Long,
        packageName: String,
    )
}

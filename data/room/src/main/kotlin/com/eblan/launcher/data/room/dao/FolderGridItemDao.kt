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
import androidx.room.Transaction
import androidx.room.Update
import androidx.room.Upsert
import com.eblan.launcher.data.room.entity.FolderGridItemEntity
import com.eblan.launcher.data.room.entity.FolderGridItemWrapperEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface FolderGridItemDao {
    @Query("SELECT * FROM FolderGridItemEntity")
    fun getFolderGridItemEntities(): List<FolderGridItemEntity>

    @Transaction
    @Query("SELECT * FROM FolderGridItemEntity")
    fun getFolderGridItemWrapperEntitiesFlow(): Flow<List<FolderGridItemWrapperEntity>>

    @Transaction
    @Query("SELECT * FROM FolderGridItemEntity WHERE id = :id")
    suspend fun getFolderGridItemWrapperEntityById(id: String): FolderGridItemWrapperEntity?

    @Upsert
    suspend fun upsertFolderGridItemEntities(entities: List<FolderGridItemEntity>)

    @Update
    suspend fun updateFolderGridItemEntity(entity: FolderGridItemEntity)

    @Delete
    suspend fun deleteFolderGridItemEntity(entity: FolderGridItemEntity)

    @Delete
    suspend fun deleteFolderGridItemEntities(entities: List<FolderGridItemEntity>)

    @Insert
    suspend fun insertFolderGridItemEntity(entity: FolderGridItemEntity)

    @Upsert
    suspend fun upsertFolderGridItemEntity(entity: FolderGridItemEntity)

    @Query("DELETE FROM FolderGridItemEntity WHERE id = :id")
    suspend fun deleteFolderGridItemEntityById(id: String)
}

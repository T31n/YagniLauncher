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
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import androidx.room.Upsert
import com.eblan.launcher.data.room.entity.FolderEblanApplicationInfoEntity
import com.eblan.launcher.data.room.entity.FolderEblanApplicationInfoWrapperEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface FolderEblanApplicationInfoDao {
    @Query("SELECT * FROM FolderEblanApplicationInfoEntity")
    fun getFolderEblanApplicationInfoEntitiesFlow(): Flow<List<FolderEblanApplicationInfoEntity>>

    @Query("SELECT * FROM FolderEblanApplicationInfoEntity")
    suspend fun getFolderEblanApplicationInfoEntities(): List<FolderEblanApplicationInfoEntity>

    @Transaction
    @Query("SELECT * FROM FolderEblanApplicationInfoEntity")
    fun getFolderEblanApplicationInfoWrapperEntitiesFlow(): Flow<List<FolderEblanApplicationInfoWrapperEntity>>

    @Transaction
    @Query("SELECT * FROM FolderEblanApplicationInfoEntity WHERE id = :id")
    suspend fun getFolderEblanApplicationInfoWrapperEntityById(id: String): FolderEblanApplicationInfoWrapperEntity?

    @Upsert
    suspend fun upsertFolderEblanApplicationInfoEntity(entity: FolderEblanApplicationInfoEntity)

    @Query("SELECT * FROM FolderEblanApplicationInfoEntity WHERE id = :id")
    suspend fun getFolderEblanApplicationInfoEntityById(id: String): FolderEblanApplicationInfoEntity?

    @Insert
    suspend fun insertFolderEblanApplicationInfoEntity(entity: FolderEblanApplicationInfoEntity)

    @Update
    suspend fun updateFolderEblanApplicationInfoEntity(entity: FolderEblanApplicationInfoEntity)
}
